package com.tuisongbao.engine.chat;

import com.google.gson.Gson;
import com.tuisongbao.engine.Engine;
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
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.http.HttpConstants;
import com.tuisongbao.engine.http.request.BaseRequest;
import com.tuisongbao.engine.http.response.BaseResponse;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.utils.ExecutorUtils;
import com.tuisongbao.engine.utils.StrUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatManager extends BaseManager {
    public static final String EVENT_MESSAGE_NEW = "message:new";
    public static final String EVENT_PRESENCE_CHANGED = "user:presenceChanged";
    public static final String EVENT_LOGIN_SUCCEEDED = "login:succeeded";
    public static final String EVENT_LOGIN_FAILED = "login:failed";

    private static final String TAG = "TSB" + ChatManager.class.getSimpleName();

    private ChatGroupManager groupManager;
    private ChatConversationManager conversationManager;
    private ChatMessageManager messageManager;

    private ChatUser mChatUser;
    private boolean mIsCacheEnabled = true;
    private String mUserData;
    private EngineCallback mAuthCallback;

    public ChatManager(Engine engine) {
        super(engine);
    }

    public ChatUser getChatUser() {
        return mChatUser;
    }

    public void login(final String userData) {
        try {
            if (StrUtils.isEqual(userData, mUserData)) {
                trigger(EVENT_LOGIN_SUCCEEDED, getChatUser());
                LogUtil.warn(TAG, "Duplicate login");
                return;
            } else {
                failedAllPendingEvents();
            }

            mAuthCallback = new EngineCallback<ChatLoginData>() {

                @Override
                public void onSuccess(ChatLoginData data) {
                    mUserData = userData;

                    LogUtil.verbose(TAG, "Auth success");
                    ChatLoginEvent event = new ChatLoginEvent();
                    event.setData(data);
                    ChatLoginEventHandler handler = new ChatLoginEventHandler();
                    send(event, handler);
                }

                @Override
                public void onError(ResponseError error) {
                    LogUtil.verbose(TAG, "Auth failed, " + error.getMessage());
                    onLogout();
                }
            };

            if (engine.getConnection().isConnected()) {
                auth(userData, mAuthCallback);
                return;
            }
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
        trigger(EVENT_LOGIN_FAILED);
    }

    public void logout() {
        try {
            if (!hasLogin()) {
                return;
            }
            onLogout();
            ChatLogoutEvent event = new ChatLogoutEvent();
            send(event, null);
        } catch (Exception e) {
            LogUtil.error(TAG, e);
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
            groupManager.clearCache();
            conversationManager.clearCache();
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
    protected void connected() {
        super.connected();

        if (hasLogin()) {
            // Auto login if connection is available.
            auth(mUserData, mAuthCallback);
        }
    }

    private JSONObject getAuthParams(String userData) {
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("socketId", engine.getConnection().getSocketId());
            requestData.put("chatLogin", true);
            if (userData != null) {
                requestData.put("authData", userData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestData;
    }

    private void auth(final String userData, final EngineCallback callback) {
        ExecutorUtils.getThreadQueue().execute(new Runnable() {

            @Override
            public void run() {
                String authRequestData = getAuthParams(userData).toString();
                BaseRequest request = new BaseRequest(HttpConstants.HTTP_METHOD_POST, engine.getEngineOptions().getAuthEndpoint(),
                        authRequestData);
                BaseResponse response = request.execute();
                if (response == null || !response.isStatusOk()) {
                    ResponseError error = new ResponseError();
                    error.setMessage("Auth failed, connection to user server error or user server feed back error");
                    callback.onError(error);
                    return;
                }
                JSONObject jsonData = response.getJSONData();
                if (jsonData == null) {
                    ResponseError error = new ResponseError();
                    error.setMessage("Auth failed, auth data from auth endpoint is empty");
                    callback.onError(error);
                    return;
                }
                ChatLoginData data = new Gson().fromJson(jsonData.toString(), ChatLoginData.class);
                callback.onSuccess(data);
            }
        });
    }

    public void onLoginSuccess(ChatUser user) {
        mChatUser = user;

        // Init groups and conversations
        groupManager = new ChatGroupManager(engine);
        conversationManager = new ChatConversationManager(engine);
        messageManager = new ChatMessageManager(engine);

        bind(Protocol.EVENT_NAME_MESSAGE_NEW, new ChatMessageNewEventHandler(engine));
        bind(Protocol.EVENT_NAME_USER_PRESENCE_CHANGE, new ChatUserPresenceChangedEventHandler(engine));

        trigger(EVENT_LOGIN_SUCCEEDED, mChatUser);
    }

    public void onLoginFailed(ResponseError error) {
        mAuthCallback = null;

        trigger(EVENT_LOGIN_FAILED, error);
    }

    public void onLogout() {
        mChatUser = null;

        mUserData = null;
        mAuthCallback = null;

        unbind(Protocol.EVENT_NAME_MESSAGE_NEW);
        unbind(Protocol.EVENT_NAME_USER_PRESENCE_CHANGE);

        unbind(EVENT_MESSAGE_NEW);
        unbind(EVENT_PRESENCE_CHANGED);

        groupManager = null;
        conversationManager = null;
    }
}
