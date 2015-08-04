package com.tuisongbao.engine.chat;

import com.google.gson.Gson;
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
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.http.HttpConstants;
import com.tuisongbao.engine.http.request.BaseRequest;
import com.tuisongbao.engine.http.response.BaseResponse;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.ExecutorUtil;

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
    private boolean mIsCacheEnabled = false;
    private String mUserData;
    private TSBEngineCallback mAuthCallback;

    public ChatManager(TSBEngine engine) {
        super(engine);

        bind(EVENT_LOGIN_SUCCEEDED, new Listener() {
            @Override
            public void call(Object... args) {
                LogUtil.info(TAG, "Login success");
                onLogin((ChatUser) args[0]);
            }
        });

        bind(EVENT_LOGIN_FAILED, new Listener() {
            @Override
            public void call(Object... args) {
                LogUtil.info(TAG, "Login failed");
                onLogout();
            }
        });
    }

    public ChatUser getChatUser() {
        return mChatUser;
    }

    public void login(final String userData) {
        try {
            if (hasLogin()) {
                trigger(EVENT_LOGIN_SUCCEEDED, getChatUser());
                LogUtil.warn(TAG, "Duplicate login");
                return;
            }

            // Cache this for auto login
            mUserData = userData;
            mAuthCallback = new TSBEngineCallback<ChatLoginData>() {

                @Override
                public void onSuccess(ChatLoginData data) {
                    LogUtil.verbose(TAG, "Auth success");
                    ChatLoginEvent event = new ChatLoginEvent();
                    event.setData(data);
                    ChatLoginEventHandler handler = new ChatLoginEventHandler();
                    send(event, handler);
                }

                @Override
                public void onError(ResponseError error) {
                    LogUtil.verbose(TAG, "Auth failed");
                    onLogout();
                }
            };

            if (engine.getConnection().isConnected()) {
                auth(userData, mAuthCallback);
            } else {
                // TODO: Set timer, trigger error if timeout.
            }
        } catch (Exception e) {

        }
    }

    public void logout() {
        try {
            if (!hasLogin()) {
                return;
            }
            if (engine.getConnection().isConnected()) {
                ChatLogoutEvent message = new ChatLogoutEvent();
                if (send(message, null)) {
                    onLogout();
                    return;
                }
            }
        } catch (Exception e) {
            LogUtil.error(TAG, "Logout failed", e);
        }
        // TODO: 15-8-4 Set timer and try to re-logout like login
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
    protected void connected() {
        if (hasLogin()) {
            // Auto login if connection is available.
            auth(mUserData, mAuthCallback);
        }
    }

    @Override
    protected void disconnected() {

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

    private void auth(final String userData, final TSBEngineCallback callback) {
        ExecutorUtil.getThreadQueue().execute(new Runnable() {

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

    private void onLogin(ChatUser user) {
        mChatUser = user;

        // Init groups and conversations
        groupManager = new ChatGroupManager(engine);
        conversationManager = new ChatConversationManager(engine);
        messageManager = new ChatMessageManager(engine);

        bind(Protocol.EVENT_NAME_MESSAGE_NEW, new ChatMessageNewEventHandler());
        bind(Protocol.EVENT_NAME_USER_PRESENCE_CHANGE, new ChatUserPresenceChangedEventHandler());
    }

    private void onLogout() {
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
