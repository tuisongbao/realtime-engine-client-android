package com.tuisongbao.engine.chat;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.entity.TSBChatLoginData;
import com.tuisongbao.engine.chat.entity.TSBChatMessageSendData;
import com.tuisongbao.engine.chat.entity.TSBChatOptions;
import com.tuisongbao.engine.chat.entity.TSBChatUser;
import com.tuisongbao.engine.chat.entity.TSBImageMessageBody;
import com.tuisongbao.engine.chat.entity.TSBMediaMessageBody;
import com.tuisongbao.engine.chat.entity.TSBMessage;
import com.tuisongbao.engine.chat.entity.TSBMessage.TYPE;
import com.tuisongbao.engine.chat.message.TSBChatLoginMessage;
import com.tuisongbao.engine.chat.message.TSBChatLoginResponseMessage;
import com.tuisongbao.engine.chat.message.TSBChatLogoutMessage;
import com.tuisongbao.engine.chat.message.TSBChatMessageGetMessage;
import com.tuisongbao.engine.chat.message.TSBChatMessageResponseMessage;
import com.tuisongbao.engine.chat.message.TSBChatMessageSendMessage;
import com.tuisongbao.engine.chat.message.TSBChatMessageResponseMessage.TSBChatMessageResponseMessageCallback;
import com.tuisongbao.engine.common.BaseManager;
import com.tuisongbao.engine.common.TSBEngineBindCallback;
import com.tuisongbao.engine.common.TSBEngineCallback;
import com.tuisongbao.engine.common.TSBEngineResponseToServerRequestMessage;
import com.tuisongbao.engine.connection.entity.TSBConnection;
import com.tuisongbao.engine.engineio.EngineConstants;
import com.tuisongbao.engine.entity.TSBEngineConstants;
import com.tuisongbao.engine.http.HttpConstants;
import com.tuisongbao.engine.http.request.BaseRequest;
import com.tuisongbao.engine.http.response.BaseResponse;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.ExecutorUtil;
import com.tuisongbao.engine.util.StrUtil;

public class TSBChatManager extends BaseManager {
    private static final String TAG = "com.tuisongbao.android.engine.TSBChatManager";
    private TSBChatUser mTSBChatUser;
    private TSBChatLoginMessage mTSBLoginMessage;

    private static TSBChatManager mInstance;
    private boolean mIsCacheEnabled = false;

    public synchronized static TSBChatManager getInstance() {
        if (mInstance == null) {
            mInstance = new TSBChatManager();
        }
        return mInstance;
    }

    private TSBChatManager() {
        super();
        // bind get message event
        TSBChatMessageResponseMessage response = new TSBChatMessageResponseMessage(
                mChatMessageCallback);
        bind(TSBChatMessageGetMessage.NAME, response);
        // bind receive new message event
        bind(EngineConstants.CHAT_NAME_NEW_MESSAGE, response);
    }

    /**
     * 聊天登录
     *
     * @param userData
     *            用户信息
     * @param callback
     */
    public void login(String userData, TSBEngineCallback<TSBChatUser> callback) {
        if (isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CHAT_CODE_LOGIN_HAS_LOGINED,
                    "user has been logined");
            return;
        }
        if (TSBEngine.isConnected()) {
            TSBChatLoginMessage message = new TSBChatLoginMessage();
            TSBChatLoginData data = new TSBChatLoginData();
            data.setUserData(userData);
            message.setData(data);
            message.setCallback(callback);
            mTSBLoginMessage = message;
            auth(message);
        } else {
            callback.onError(
                    TSBEngineConstants.CONNECTION_CODE_CONNECTION_SEND_MESSAGE_FAILED,
                    "can't connect to engine server");
        }
    }

    public TSBChatUser getChatUser() {
        return mTSBChatUser;
    }

    /**
     * 退出登录
     *
     */
    public void logout() {
        if (!isLogin()) {
            clearCacheUser();
            return;
        }
        if (TSBEngine.isConnected()) {
            TSBChatLogoutMessage message = new TSBChatLogoutMessage();
            send(message);
        }
        clearCacheUser();
    }

    public void enableCache() {
        mIsCacheEnabled = true;
    }

    public boolean isCacheEnabled() {
        return mIsCacheEnabled;
    }

    public void clearCache() {
        try {
            TSBConversationManager.getInstance().clearCache();
            TSBGroupManager.getInstance().clearCache();
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
    public void sendMessage(final TSBMessage message,
            final TSBEngineCallback<TSBMessage> callback, TSBChatOptions options) {
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
            handleErrorMessage(callback, EngineConstants.ENGINE_CODE_UNKNOWN, EngineConstants.ENGINE_MESSAGE_UNKNOWN_ERROR);
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }
    }

    public void bind(String bindName, TSBEngineBindCallback callback) {
        if (StrUtil.isEmpty(bindName) || callback == null) {
            return;
        }
        super.bind(bindName, callback);
    }

    public void unbind(String bindName, TSBEngineBindCallback callback) {
        if (StrUtil.isEmpty(bindName) || callback == null) {
            return;
        }
        super.unbind(bindName, callback);
    }

    @Override
    protected void handleConnect(TSBConnection t) {
        // when logined, it need to re-login
        if (isLogin() && mTSBLoginMessage != null) {
            // 当断掉重连时不需要回调
            auth(mTSBLoginMessage);
        }
    }

    @Override
    protected void handleDisconnect(int code, String message) {
        // empty
    }

    private void sendMessageRequest(TSBMessage message, TSBEngineCallback<TSBMessage> callback) {
        TSBChatMessageSendMessage request = new TSBChatMessageSendMessage();
        TSBChatMessageSendData data = new TSBChatMessageSendData();
        data.setTo(message.getRecipient());
        data.setType(message.getChatType());
        data.setContent(message.getBody());
        request.setData(data);

        TSBChatMessageResponseMessage response = new TSBChatMessageResponseMessage(
                mChatMessageCallback);
        response.setSendMessage(message);
        response.setCustomCallback(callback);
        send(request, response);
    }

    private void sendMediaMessage(final TSBMessage message, final TSBEngineCallback<TSBMessage> callback, TSBChatOptions options) {
        TSBEngineCallback<JSONObject> handlerCallback = getUploaderHandlerOfMediaMessage(message, callback);
        if (!uploadMessageResourceToQiniu(message, handlerCallback, options)) {
            callback.onError(EngineConstants.CHANNEL_CODE_INVALID_OPERATION_ERROR, "Failed to get resource of the message.");
        }
    }

    private boolean uploadMessageResourceToQiniu(TSBMessage message, final TSBEngineCallback<JSONObject> responseHandler,
            final TSBChatOptions options) {
        TSBMediaMessageBody mediaBody = (TSBMediaMessageBody)message.getBody();
        String filePath = mediaBody.getLocalPath();
        if (StrUtil.isEmpty(filePath)) {
            return false;
        }

        UploadManager manager = new UploadManager();
        String token = TSBChatManager.getInstance().getChatUser().getUploadToken(message.getBody().getType().getName());

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
                if (responseObject == null) {
                    responseHandler.onError(EngineConstants.ENGINE_CODE_UNKNOWN, "Failed to upload image to TuiSongBao, please try later");
                } else {
                    responseHandler.onSuccess(responseObject);
                }
            }
        }, opt);
        return true;
    }

    private TSBEngineCallback<JSONObject> getUploaderHandlerOfMediaMessage(final TSBMessage message,
            final TSBEngineCallback<TSBMessage> callback) {
        TSBEngineCallback<JSONObject> responseHandler = new TSBEngineCallback<JSONObject>() {

            @Override
            public void onSuccess(JSONObject responseObject) {
                try {
                    LogUtil.info(LogUtil.LOG_TAG_CHAT, "Get response from QINIU " + responseObject.toString(4));
                    TSBMediaMessageBody body = (TSBMediaMessageBody) message.getBody();

                    JsonObject file = new JsonObject();
                    file.addProperty(TSBImageMessageBody.KEY, responseObject.getString("key"));
                    file.addProperty("persistentId", responseObject.getString("persistentId"));
//                    file.addProperty(TSBImageMessageBody.ETAG, responseObject.getString("etag"));
//                    file.addProperty(TSBImageMessageBody.NAME, responseObject.getString("fname"));
//                    file.addProperty(TSBImageMessageBody.SIZE, responseObject.getString("fsize"));
//                    file.addProperty(TSBImageMessageBody.MIME_TYPE, responseObject.getString("mimeType"));
//
//                    TYPE messageType = body.getType();
//                    if (messageType == TYPE.IMAGE) {
//                        JSONObject imageInfoInResponse = responseObject.getJSONObject("imageInfo");
//                        file.addProperty(TSBImageMessageBody.IMAGE_INFO_WIDTH, imageInfoInResponse.getInt("width"));
//                        file.addProperty(TSBImageMessageBody.IMAGE_INFO_HEIGHT, imageInfoInResponse.getInt("height"));
//                        body.setFile(file);
//                    } else if (messageType == TYPE.VOICE || messageType == TYPE.VIDEO) {
//                        JSONObject formatInfoInResponse = responseObject.getJSONObject("avinfo").getJSONObject("format");
//                        file.addProperty(TSBVoiceMessageBody.VOICE_INFO_DURATION, formatInfoInResponse.getString("duration"));
//                        body.setFile(file);
//                    }
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

    private void handleErrorMessage(TSBChatLoginMessage msg, int code,
            String message) {
        handleErrorMessage(msg.getCallback(), code, message);
    }

    private void auth(final TSBChatLoginMessage msg) {
        ExecutorUtil.getThreadQueue().execute(new Runnable() {

            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    json.put("socketId", TSBEngine.getSocketId());
                    json.put("chatLogin", true);
                    if (msg.getData() != null
                            && !StrUtil.isEmpty(msg.getData().getUserData())) {
                        json.put("authData", msg.getData().getUserData());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                BaseRequest request = new BaseRequest(
                        HttpConstants.HTTP_METHOD_POST, TSBEngine
                                .getTSBEngineOptions().getAuthEndpoint(), json
                                .toString());
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
                        TSBChatLoginMessage authMessage = new TSBChatLoginMessage();
                        TSBChatLoginData authData = new TSBChatLoginData();
                        authData.setUserData(userData);
                        authData.setSignature(signature);
                        authMessage.setData(authData);
                        TSBChatLoginResponseMessage responseMessage = new TSBChatLoginResponseMessage();
                        responseMessage.setCallback(mLoginCallback);
                        send(authMessage, responseMessage);
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

    private TSBEngineCallback<TSBChatUser> mLoginCallback = new TSBEngineCallback<TSBChatUser>() {
        @Override
        public void onSuccess(TSBChatUser t) {
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
            } else {
                // Chat user is not null, update user's informations, like isNew, uploadToken
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

    private TSBChatMessageResponseMessageCallback mChatMessageCallback = new TSBChatMessageResponseMessageCallback() {

        @Override
        public void onEvent(TSBChatMessageResponseMessage response) {
            if (response.isSuccess()) {
                if (TSBChatMessageGetMessage.NAME
                        .equals(response.getBindName())) {
                    // 当为获取消息时
                    // response to server
                    long serverRequestId = response.getServerRequestId();
                    TSBEngineResponseToServerRequestMessage message = new TSBEngineResponseToServerRequestMessage(
                            serverRequestId, true);
                    send(message);
                } else if (TSBChatMessageSendMessage.NAME.equals(response
                        .getBindName())) {
                    if (response.getCustomCallback() != null) {
                        // 发送消息成功
                        TSBMessage message = response.getSendMessage();
                        try {
                            JSONObject json = new JSONObject(response.getData());
                            long messageId = json.optLong("messageId");
                            message.setMessageId(messageId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        response.getCustomCallback().onSuccess(response.getCallBackData());
                    }
                } else if (EngineConstants.CHAT_NAME_NEW_MESSAGE
                        .equals(response.getBindName())) {
                    // 接收到消息
                    if (response.getData() != null) {
                        // response to server
                        long serverRequestId = response.getServerRequestId();
                        TSBEngineResponseToServerRequestMessage request = new TSBEngineResponseToServerRequestMessage(
                                serverRequestId, true);
                        TSBMessage message = response.getCallBackData();
                        try {
                            JSONObject result = new JSONObject();
                            result.put("messageId", message.getMessageId());
                            request.setResult(result);
                            send(request);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                // 发送消息失败
                if (TSBChatMessageSendMessage.NAME.equals(response
                        .getBindName())) {
                    if (response.getCustomCallback() != null) {
                        response.getCustomCallback().onError(response.getCode(), response.getErrorMessage());
                    }
                }
            }

        }
    };
}
