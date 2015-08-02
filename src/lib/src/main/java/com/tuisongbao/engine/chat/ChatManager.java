package com.tuisongbao.engine.chat;

import android.util.Log;

import com.google.gson.JsonObject;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.conversation.ChatConversationManager;
import com.tuisongbao.engine.chat.group.ChatGroupManager;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.event.ChatMessageGetEvent;
import com.tuisongbao.engine.chat.message.event.ChatMessageSendEvent;
import com.tuisongbao.engine.chat.user.entity.ChatLoginData;
import com.tuisongbao.engine.chat.message.entity.ChatMessageSendData;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.chat.message.entity.ChatImageMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatMediaMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatMessage.TYPE;
import com.tuisongbao.engine.chat.message.entity.ChatVoiceMessageBody;
import com.tuisongbao.engine.chat.user.event.ChatLoginEvent;
import com.tuisongbao.engine.chat.user.event.handler.ChatLoginEventHandler;
import com.tuisongbao.engine.chat.user.event.ChatLogoutEvent;
import com.tuisongbao.engine.common.BaseManager;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.connection.entity.ConnectionEventData;
import com.tuisongbao.engine.common.TSBEngineConstants;
import com.tuisongbao.engine.http.HttpConstants;
import com.tuisongbao.engine.http.request.BaseRequest;
import com.tuisongbao.engine.http.response.BaseResponse;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.ExecutorUtil;
import com.tuisongbao.engine.util.StrUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChatManager extends BaseManager {
    public static ChatGroupManager groupManager;
    public static ChatConversationManager conversationManager;

    private static final String TAG = ChatManager.class.getSimpleName();

    private ChatUser mTSBChatUser;
    private ChatLoginEvent mTSBLoginMessage;
    private boolean mIsCacheEnabled = false;

    public ChatManager(TSBEngine engine) {
        super(engine);
        groupManager = new ChatGroupManager(this);
        conversationManager = new ChatConversationManager(this);
    }

    public boolean isLogin() {
        return mTSBChatUser != null;
    }

    /**
     * 聊天登录
     *
     * @param userData
     *            用户信息
     * @param callback
     */
    public void login(String userData, TSBEngineCallback<ChatUser> callback) {
        if (isLogin()) {
            callback.onSuccess(getChatUser());
            LogUtil.warn(TAG, "Duplicate login");
            return;
        }
        // TODO: Cache login message, re-login when connection is established
        if (engine.connection.isConnected()) {
            ChatLoginEvent message = new ChatLoginEvent();
            ChatLoginData data = new ChatLoginData();
            data.setUserData(userData);
            message.setData(data);
            message.setCallback(callback);
            mTSBLoginMessage = message;
            auth(message);
        } else {
            // TODO: Set timer, callback error if timeout.
            callback.onError(
                    TSBEngineConstants.CONNECTION_CODE_CONNECTION_SEND_MESSAGE_FAILED,
                    "can't connect to engine server");
        }
    }

    public ChatUser getChatUser() {
        return mTSBChatUser;
    }

    /**
     * 退出登录
     *
     */
    public void logout() {
        try {
            if (!isLogin()) {
                clearCacheUser();
                return;
            }
            if (engine.connection.isConnected()) {
                ChatLogoutEvent message = new ChatLogoutEvent();
                send(message, null);
            }
            clearCacheUser();
        } catch (Exception e) {
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
            if (!isLogin()) {
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
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }
    }

    @Override
    protected void handleConnect(ConnectionEventData t) {
        if (isLogin() && mTSBLoginMessage != null) {
            // TODO: 当断掉重连时不需要回调???
            auth(mTSBLoginMessage);
        }
    }

    @Override
    protected void handleDisconnect(int code, String message) {
        // empty
    }

    private void sendMessageRequest(ChatMessage message, TSBEngineCallback<ChatMessage> callback) throws JSONException {
        ChatMessageSendEvent request = new ChatMessageSendEvent();
        ChatMessageSendData data = new ChatMessageSendData();
        data.setTo(message.getRecipient());
        data.setType(message.getChatType());
        data.setContent(message.getBody());
        request.setData(data);

        send(request, null);
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
        String token = mTSBChatUser.getUploadToken();
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
                    LogUtil.info(LogUtil.LOG_TAG_CHAT, "Get response from QINIU " + responseObject.toString(4));
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
                    LogUtil.error(LogUtil.LOG_TAG_CHAT, e);
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
        mTSBChatUser = null;
        mTSBLoginMessage = null;
    }

    private void handleErrorMessage(ChatLoginEvent msg, int code,
            String message) {
        handleErrorMessage(msg.getCallback(), code, message);
    }

    private void auth(final ChatLoginEvent msg) {
        ExecutorUtil.getThreadQueue().execute(new Runnable() {

            @Override
            public void run() {
                JSONObject requestData = new JSONObject();
                try {
                    requestData.put("socketId", engine.connection.getSocketId());
                    requestData.put("chatLogin", true);
                    if (msg.getData() != null
                            && !StrUtil.isEmpty(msg.getData().getUserData())) {
                        requestData.put("authData", msg.getData().getUserData());
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
                                msg,
                                TSBEngineConstants.TSBENGINE_CHAT_CODE_LOGIN_FAILED,
                                "auth failed, feed back auth data is empty");
                    } else {
                        String signature = jsonData.optString("signature");
                        if (StrUtil.isEmpty(signature)) {
                            // signature data empty
                            handleErrorMessage(
                                    msg,
                                    TSBEngineConstants.TSBENGINE_CHAT_CODE_LOGIN_FAILED,
                                    "auth failed, signature is empty");
                        } else {
                            // empty
                        }
                        String userData = jsonData.optString("userData");

                        ChatLoginEvent loginEvent = new ChatLoginEvent();
                        ChatLoginData authData = new ChatLoginData();
                        authData.setUserData(userData);
                        authData.setSignature(signature);
                        loginEvent.setData(authData);
                        ChatLoginEventHandler responseMessage = new ChatLoginEventHandler();
                        responseMessage.setCallback(mLoginCallback);
                        if (!send(loginEvent, responseMessage)) {
                            // TODO: 15-7-31 Cache and re-send when connected
                        }
                    }
                } else {
                    // connection to user server error or user server feed back
                    // error
                    handleErrorMessage(
                            msg,
                            TSBEngineConstants.TSBENGINE_CHAT_CODE_LOGIN_FAILED,
                            "auth failed, connection to user server error or user server feed back error");
                }
            }
        });
    }

    private TSBEngineCallback<ChatUser> mLoginCallback = new TSBEngineCallback<ChatUser>() {
        @Override
        public void onSuccess(ChatUser t) {
            LogUtil.debug(LogUtil.LOG_TAG_CHAT, t.toString());
            if (!isLogin()) {
                // Chat user is null
                mTSBChatUser = t;
                mTSBChatUser.setUserId(mTSBLoginMessage.getData().getUserData());
                // Call back when the user first login
                if (mTSBLoginMessage != null && mTSBLoginMessage.getCallback() != null) {
                    LogUtil.debug(LogUtil.LOG_TAG_CHAT, t.toString());
                    mTSBLoginMessage.getCallback().onSuccess(t);
                }

                bind(ChatMessageGetEvent.NAME, new Listener() {
                    @Override
                    public void call(Object... args) {

                    }
                });

                bind(Protocol.EVENT_NAME_MESSAGE_NEW, new Listener() {
                    @Override
                    public void call(Object... args) {

                    }
                });

                bind(ChatMessageSendEvent.NAME, new Listener() {
                    @Override
                    public void call(Object... args) {

                    }
                });
            } else {
                // Chat user is not null, update user's information, like isNew, uploadToken
                mTSBChatUser = t;
                // `userId` field is not set in server response
                mTSBChatUser.setUserId(mTSBLoginMessage.getData().getUserData());
                // No need to callback when auto login finished
            }
        }

        @Override
        public void onError(int code, String message) {
            if (mTSBLoginMessage != null && mTSBLoginMessage.getCallback() != null) {
                mTSBLoginMessage.getCallback().onError(code, message);
            }
        }
    };
}
