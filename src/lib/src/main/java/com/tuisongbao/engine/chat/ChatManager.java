package com.tuisongbao.engine.chat;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.conversation.ChatConversationManager;
import com.tuisongbao.engine.chat.group.ChatGroupManager;
import com.tuisongbao.engine.chat.message.ChatMessageManager;
import com.tuisongbao.engine.chat.message.event.handler.ChatMessageNewEventHandler;
import com.tuisongbao.engine.chat.user.entity.ChatLoginData;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.chat.user.event.ChatLoginEvent;
import com.tuisongbao.engine.chat.user.event.ChatLogoutEvent;
import com.tuisongbao.engine.chat.user.event.handler.ChatLoginEventHandler;
import com.tuisongbao.engine.chat.user.event.handler.ChatUserPresenceChangedEventHandler;
import com.tuisongbao.engine.common.BaseManager;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.TSBEngineConstants;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.connection.entity.ConnectionEventData;
import com.tuisongbao.engine.http.HttpConstants;
import com.tuisongbao.engine.http.request.BaseRequest;
import com.tuisongbao.engine.http.response.BaseResponse;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.ExecutorUtil;
import com.tuisongbao.engine.util.StrUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatManager extends BaseManager {
    public static final String EVENT_MESSAGE_NEW = "message:new";
    public static final String EVENT_PRESENCE_CHANGED = "user:presenceChanged";

    private static final String TAG = ChatManager.class.getSimpleName();

    private ChatGroupManager groupManager;
    private ChatConversationManager conversationManager;
    private ChatMessageManager messageManager;
    private ChatUser mChatUser;
    private ChatLoginEvent mLoginEvent;
    private boolean mIsCacheEnabled = false;

    public ChatManager(TSBEngine engine) {
        super(engine);
    }

    /**
     * 聊天登录
     *
     * @param userData
     *            用户信息
     * @param callback
     */
    public void login(String userData, TSBEngineCallback<ChatUser> callback) {
        if (hasLogin()) {
            callback.onSuccess(getChatUser());
            LogUtil.warn(TAG, "Duplicate login");
            return;
        }
        // TODO: Cache login message, re-login when connection is established
        if (engine.getConnection().isConnected()) {
            ChatLoginEvent event = new ChatLoginEvent();
            ChatLoginData data = new ChatLoginData();
            data.setUserData(userData);
            event.setData(data);
            event.setCallback(callback);
            mLoginEvent = event;
            auth(event);
        } else {
            // TODO: Set timer, onResponse error if timeout.
            callback.onError(
                    TSBEngineConstants.CONNECTION_CODE_CONNECTION_SEND_MESSAGE_FAILED,
                    "can't connect to engine server");
        }
    }

    public ChatUser getChatUser() {
        return mChatUser;
    }

    /**
     * 退出登录
     *
     */
    public void logout() {
        try {
            if (!hasLogin()) {
                clearCacheUser();
                return;
            }
            if (engine.getConnection().isConnected()) {
                ChatLogoutEvent message = new ChatLogoutEvent();
                if (send(message, null)) {
                    onLogout();
                }
            }
            onLogout();
        } catch (Exception e) {
            // TODO: 15-8-2 What to do if logout failed ?
            LogUtil.error(TAG, "Logout failed", e);
        }
    }

    public void enableCache() {
        mIsCacheEnabled = true;
    }

    public void disableCache() {
        mIsCacheEnabled = false;
    }

    public boolean isCacheEnabled() {
        return mIsCacheEnabled;
    }

    public void clearCache() {
        try {
            // TODO: Clear group and conversation data
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
    }

    public boolean hasLogin() {
        return mChatUser != null;
    }

    public ChatConversationManager getConversationManager() {
        return conversationManager;
    }

    public ChatGroupManager getGroupManager() {
        return groupManager;
    }

    public ChatMessageManager getMessageManager() {
        return messageManager;
    }

    @Override
    protected void handleConnect(ConnectionEventData t) {
        if (hasLogin() && mLoginEvent != null) {
            // TODO: 当断掉重连时不需要回调???
            auth(mLoginEvent);
        }
    }

    @Override
    protected void handleDisconnect(int code, String message) {
        // empty
    }

    private void clearCacheUser() {
        mChatUser = null;
        mLoginEvent = null;
    }

    private JSONObject getAuthParams(ChatLoginEvent event) {
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("socketId", engine.getConnection().getSocketId());
            requestData.put("chatLogin", true);
            if (event.getData() != null
                    && !StrUtil.isEmpty(event.getData().getUserData())) {
                requestData.put("authData", event.getData().getUserData());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestData;
    }

    private void auth(final ChatLoginEvent event) {
        ExecutorUtil.getThreadQueue().execute(new Runnable() {

            @Override
            public void run() {
                String authRequestData = getAuthParams(event).toString();
                BaseRequest request = new BaseRequest(HttpConstants.HTTP_METHOD_POST, engine.getEngineOptions().getAuthEndpoint(),
                        authRequestData);
                BaseResponse response = request.execute();
                if (response == null || !response.isStatusOk()) {
                    handleErrorMessage(event.getCallback(), TSBEngineConstants.TSBENGINE_CHAT_CODE_LOGIN_FAILED,
                            "auth failed, connection to user server error or user server feed back error");
                    return;
                }
                JSONObject jsonData = response.getJSONData();
                if (jsonData == null) {
                    handleErrorMessage(event.getCallback(),
                            TSBEngineConstants.TSBENGINE_CHAT_CODE_LOGIN_FAILED,
                            "auth failed, auth data from auth endpoint is empty");
                    return;
                }

                String signature = jsonData.optString("signature");
                if (StrUtil.isEmpty(signature)) {
                    // signature data empty
                    handleErrorMessage(event.getCallback(),
                            TSBEngineConstants.TSBENGINE_CHAT_CODE_LOGIN_FAILED,
                            "auth failed, signature is empty");
                    return;
                }
                String userData = jsonData.optString("userData");
                ChatLoginEvent loginEvent = new ChatLoginEvent();
                ChatLoginData data = new ChatLoginData();
                data.setSignature(signature);
                data.setUserData(userData);
                loginEvent.setData(data);
                ChatLoginEventHandler responseMessage = new ChatLoginEventHandler();
                if (!send(loginEvent, responseMessage)) {
                    // TODO: 15-7-31 Cache and re-send when connected
                }
            }
        });
    }

    public void onLogin(ChatUser user) {
        mChatUser = user;

        // Init groups and conversations
        groupManager = new ChatGroupManager(engine);
        conversationManager = new ChatConversationManager(engine);
        messageManager = new ChatMessageManager(engine);

        bind(Protocol.EVENT_NAME_MESSAGE_NEW, new ChatMessageNewEventHandler());
        bind(Protocol.EVENT_NAME_USER_PRESENCE_CHANGE, new ChatUserPresenceChangedEventHandler());
    }

    private void onLogout() {
        clearCacheUser();
        unbind(Protocol.EVENT_NAME_MESSAGE_NEW);
        unbind(Protocol.EVENT_NAME_USER_PRESENCE_CHANGE);

        unbind(EVENT_MESSAGE_NEW);
        unbind(EVENT_PRESENCE_CHANGED);

        groupManager = null;
        conversationManager = null;
    }
}
