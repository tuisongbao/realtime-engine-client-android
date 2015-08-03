package com.tuisongbao.engine.chat;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.google.gson.JsonObject;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.conversation.ChatConversationManager;
import com.tuisongbao.engine.chat.group.ChatGroupManager;
import com.tuisongbao.engine.chat.message.entity.ChatImageMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatMediaMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessage.TYPE;
import com.tuisongbao.engine.chat.message.entity.ChatVoiceMessageBody;
import com.tuisongbao.engine.chat.message.event.ChatMessageSendEvent;
import com.tuisongbao.engine.chat.message.event.handler.ChatMessageNewEventHandler;
import com.tuisongbao.engine.chat.message.event.handler.ChatMessageSendEventHandler;
import com.tuisongbao.engine.chat.user.entity.ChatLoginData;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.chat.user.event.ChatLoginEvent;
import com.tuisongbao.engine.chat.user.event.ChatLogoutEvent;
import com.tuisongbao.engine.chat.user.event.handler.ChatLoginEventHandler;
import com.tuisongbao.engine.common.BaseManager;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.TSBEngineConstants;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.event.handler.IEventHandler;
import com.tuisongbao.engine.connection.entity.ConnectionEventData;
import com.tuisongbao.engine.http.HttpConstants;
import com.tuisongbao.engine.http.request.BaseRequest;
import com.tuisongbao.engine.http.response.BaseResponse;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.ExecutorUtil;
import com.tuisongbao.engine.util.StrUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatManager extends BaseManager {
    private static final String TAG = ChatManager.class.getSimpleName();

    private ChatGroupManager groupManager;
    private ChatConversationManager conversationManager;
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
                    // TODO: 15-8-3 Unbind listeners, clear cache
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

    /**
     * 发送消息
     *
     * @param message
     *            消息
     * @param callback
     */
    public void sendMessage(final ChatMessage message,
            final TSBEngineCallback<ChatMessage> callback, ChatOptions options) {
        try {
            if (!hasLogin()) {
                handleErrorMessage(callback,
                        TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                        "permission denny: need to login");
                return;
            }
            if (message == null) {
                handleErrorMessage(callback,
                        TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                        "illegal parameter: message can't not be empty");
                return;
            }
            if (message.getChatType() == null) {
                handleErrorMessage(callback,
                        TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                        "illegal parameter: message chat type can't not be empty");
                return;
            }
            if (StrUtil.isEmpty(message.getRecipient())) {
                handleErrorMessage(callback,
                        TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                        "illegal parameter: message recipient id can't not be empty");
                return;
            }
            if (message.getBody() == null) {
                handleErrorMessage(callback,
                        TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                        "illegal parameter: message body can't not be empty");
                return;
            }

            TYPE messageType = message.getBody().getType();
            if (messageType == TYPE.TEXT) {
                sendMessageRequest(message, callback);
            } else {
                sendMediaMessage(message, callback, options);
            }
        } catch (Exception e) {
            handleErrorMessage(callback, Protocol.ENGINE_CODE_UNKNOWN, Protocol.ENGINE_MESSAGE_UNKNOWN_ERROR);
            LogUtil.error(TAG, e);
        }
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

    private void sendMessageRequest(ChatMessage message, TSBEngineCallback<ChatMessage> callback) throws JSONException {
        ChatMessageSendEvent request = new ChatMessageSendEvent();
        message.setCreatedAt(StrUtil.getTimeStringIOS8061(new Date()));
        request.setData(message);
        ChatMessageSendEventHandler handler = new ChatMessageSendEventHandler();
        handler.setCallback(callback);

        send(request, handler);
    }

    private void sendMediaMessage(final ChatMessage message, final TSBEngineCallback<ChatMessage> callback, ChatOptions options) {
        TSBEngineCallback<JSONObject> handlerCallback = getUploaderHandlerOfMediaMessage(message, callback);
        if (!uploadMessageResourceToQiniu(message, handlerCallback, options)) {
            callback.onError(Protocol.CHANNEL_CODE_INVALID_OPERATION_ERROR, "Failed to get resource of the message.");
        }
    }

    private boolean uploadMessageResourceToQiniu(ChatMessage message, final TSBEngineCallback<JSONObject> responseHandler,
            final ChatOptions options) {
        ChatMediaMessageBody mediaBody = (ChatMediaMessageBody)message.getBody();
        String filePath = mediaBody.getLocalPath();
        if (StrUtil.isEmpty(filePath)) {
            return false;
        }

        UploadManager manager = new UploadManager();
        String token = mChatUser.getUploadToken();
        Log.d(TAG, token);
        UpProgressHandler progressHandler = null;
        if (options != null) {
            progressHandler = new UpProgressHandler() {

                @Override
                public void progress(String arg0, double percent) {
                    options.callbackProgress((int)(percent * 100));
                }
            };
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("x:targetId", message.getRecipient());
        final UploadOptions opt = new UploadOptions(params, null, true, progressHandler, null);
        manager.put(filePath, null, token, new UpCompletionHandler() {

            @Override
            public void complete(String key, ResponseInfo info, JSONObject responseObject) {
                Log.i(TAG, "Get response of qiniu, info: " + info.isOK() + " error: " + info.error);
                if (!info.isOK()) {
                    responseHandler.onError(Protocol.ENGINE_CODE_UNKNOWN, info.error);
                } else {
                    responseHandler.onSuccess(responseObject);
                }
            }
        }, opt);
        return true;
    }

    private TSBEngineCallback<JSONObject> getUploaderHandlerOfMediaMessage(final ChatMessage message,
            final TSBEngineCallback<ChatMessage> callback) {
        TSBEngineCallback<JSONObject> responseHandler = new TSBEngineCallback<JSONObject>() {

            @Override
            public void onSuccess(JSONObject responseObject) {
                try {
                    LogUtil.info(TAG, "Get response from QINIU " + responseObject.toString(4));
                    ChatMediaMessageBody body = (ChatMediaMessageBody) message.getBody();

                    JsonObject file = new JsonObject();
                    file.addProperty(ChatImageMessageBody.KEY, responseObject.getString("key"));
                    file.addProperty(ChatImageMessageBody.ETAG, responseObject.getString("etag"));
                    file.addProperty(ChatImageMessageBody.NAME, responseObject.getString("fname"));
                    file.addProperty(ChatImageMessageBody.SIZE, responseObject.getString("fsize"));
                    file.addProperty(ChatImageMessageBody.MIME_TYPE, responseObject.getString("mimeType"));

                    TYPE messageType = body.getType();
                    if (messageType == TYPE.IMAGE) {
                        JSONObject imageInfoInResponse = responseObject.getJSONObject("imageInfo");
                        file.addProperty(ChatImageMessageBody.IMAGE_INFO_WIDTH, imageInfoInResponse.getInt("width"));
                        file.addProperty(ChatImageMessageBody.IMAGE_INFO_HEIGHT, imageInfoInResponse.getInt("height"));
                        body.setFile(file);
                    } else if (messageType == TYPE.VOICE || messageType == TYPE.VIDEO) {
                        JSONObject formatInfoInResponse = responseObject.getJSONObject("avinfo").getJSONObject("format");
                        file.addProperty(ChatVoiceMessageBody.VOICE_INFO_DURATION, formatInfoInResponse.getString("duration"));
                        body.setFile(file);
                    }
                    body.setFile(file);
                    message.setBody(body);
                    sendMessageRequest(message, callback);
                } catch (Exception e) {
                    LogUtil.error(TAG, e);
                }
            }

            @Override
            public void onError(int code, String message) {
                callback.onError(code, message);
            }
        };
        return responseHandler;
    }

    private void clearCacheUser() {
        mChatUser = null;
        mLoginEvent = null;
    }

    private void handleErrorMessage(ChatLoginEvent event, int code,
            String message) {
        handleErrorMessage(event.getCallback(), code, message);
    }

    private void auth(final ChatLoginEvent event) {
        ExecutorUtil.getThreadQueue().execute(new Runnable() {

            @Override
            public void run() {
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
                BaseRequest request = new BaseRequest(
                        HttpConstants.HTTP_METHOD_POST
                        , engine.getEngineOptions().getAuthEndpoint()
                        , requestData.toString());
                BaseResponse response = request.execute();
                if (response != null && response.isStatusOk()) {
                    JSONObject jsonData = response.getJSONData();
                    if (jsonData == null) {
                        // feed back empty
                        handleErrorMessage(
                                event,
                                TSBEngineConstants.TSBENGINE_CHAT_CODE_LOGIN_FAILED,
                                "auth failed, feed back auth data is empty");
                    } else {
                        String signature = jsonData.optString("signature");
                        if (StrUtil.isEmpty(signature)) {
                            // signature data empty
                            handleErrorMessage(
                                    event,
                                    TSBEngineConstants.TSBENGINE_CHAT_CODE_LOGIN_FAILED,
                                    "auth failed, signature is empty");
                        } else {
                            // empty
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
                } else {
                    // connection to user server error or user server feed back
                    // error
                    handleErrorMessage(event, TSBEngineConstants.TSBENGINE_CHAT_CODE_LOGIN_FAILED,
                            "auth failed, connection to user server error or user server feed back error");
                }
            }
        });
    }

    public void onLoginSuccess(ChatUser user) {
        mChatUser = user;

        // Init groups and conversations
        groupManager = new ChatGroupManager(engine);
        conversationManager = new ChatConversationManager(engine);

        bind(Protocol.EVENT_NAME_MESSAGE_NEW, new ChatMessageNewEventHandler());
    }

    private void onLogout() {
        clearCacheUser();
        unbind(Protocol.EVENT_NAME_MESSAGE_NEW);

        groupManager = null;
        conversationManager = null;
    }

    protected void bind(final String eventName, final IEventHandler response) {
        bind(eventName, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                RawEvent rawEvent = new RawEvent(eventName);
                response.onResponse(rawEvent, (RawEvent)args[0]);
            }
        });
    }
}
