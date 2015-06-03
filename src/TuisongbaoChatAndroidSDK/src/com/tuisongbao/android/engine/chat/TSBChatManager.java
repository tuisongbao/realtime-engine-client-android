package com.tuisongbao.android.engine.chat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.entity.TSBChatLoginData;
import com.tuisongbao.android.engine.chat.entity.TSBChatMessageSendData;
import com.tuisongbao.android.engine.chat.entity.TSBChatUser;
import com.tuisongbao.android.engine.chat.entity.TSBImageMessageBody;
import com.tuisongbao.android.engine.chat.entity.TSBMediaMessageBody;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.entity.TSBMessage.TYPE;
import com.tuisongbao.android.engine.chat.entity.TSBVoiceMessageBody;
import com.tuisongbao.android.engine.chat.message.TSBChatLoginMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatLoginResponseMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatLogoutMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatMessageGetMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatMessageResponseMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatMessageResponseMessage.TSBChatMessageResponseMessageCallback;
import com.tuisongbao.android.engine.chat.message.TSBChatMessageSendMessage;
import com.tuisongbao.android.engine.common.BaseManager;
import com.tuisongbao.android.engine.common.TSBEngineBindCallback;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.common.TSBEngineResponseToServerRequestMessage;
import com.tuisongbao.android.engine.connection.entity.TSBConnection;
import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.entity.TSBEngineConstants;
import com.tuisongbao.android.engine.http.HttpConstants;
import com.tuisongbao.android.engine.http.request.BaseRequest;
import com.tuisongbao.android.engine.http.response.BaseResponse;
import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.util.ExecutorUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBChatManager extends BaseManager {

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
            clearCache();
            return;
        }
        if (TSBEngine.isConnected()) {
            TSBChatLogoutMessage message = new TSBChatLogoutMessage();
            send(message);
        }
        clearCache();
    }

    public void enableCache() {
        mIsCacheEnabled = true;
    }

    public boolean isCacheEnabled() {
        return mIsCacheEnabled;
    }

    /**
     * 发送消息
     *
     * @param message
     *            消息
     * @param callback
     */
    public void sendMessage(final TSBMessage message,
            final TSBEngineCallback<TSBMessage> callback) {
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
                sendMediaMessage(message, callback);
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

    private void sendImageMessage(final TSBMessage message, final TSBEngineCallback<TSBMessage> callback) {
        TSBImageMessageBody imageBody = (TSBImageMessageBody)message.getBody();
        String filePath = imageBody.getLocalPath();
        if (StrUtil.isEmpty(filePath)) {
            callback.onError(EngineConstants.CHANNEL_CODE_INVALID_OPERATION_ERROR, "File path is invalid.");
            return;
        }

        UploadManager manager = new UploadManager();
        String token = TSBChatManager.getInstance().getChatUser().getUploadToken();

        Map<String, String> params = new HashMap<String, String>();
        params.put("x:targetId", message.getRecipient());
        final UploadOptions opt = new UploadOptions(params, null, true, null, null);
        manager.put(filePath, null, token, new UpCompletionHandler() {

            @Override
            public void complete(String key, ResponseInfo info, JSONObject responseObject) {
                LogUtil.info(LogUtil.LOG_TAG_CHAT, "Upload file finished with key: " + key + ", info: " + info);

                try {
                    LogUtil.info(LogUtil.LOG_TAG_CHAT, "Get response from QINIU " + responseObject.toString(4));
                    TSBImageMessageBody body = (TSBImageMessageBody) message.getBody();

                    JsonObject file = new JsonObject();
                    file.addProperty(TSBImageMessageBody.KEY, responseObject.getString("key"));
                    file.addProperty(TSBImageMessageBody.ETAG, responseObject.getString("etag"));
                    file.addProperty(TSBImageMessageBody.NAME, responseObject.getString("fname"));
                    file.addProperty(TSBImageMessageBody.SIZE, responseObject.getString("fsize"));
                    file.addProperty(TSBImageMessageBody.MIME_TYPE, responseObject.getString("mimeType"));

                    JSONObject imageInfoInResponse = responseObject.getJSONObject("imageInfo");
                    JsonObject imageInfo = new JsonObject();
                    imageInfo.addProperty(TSBImageMessageBody.IMAGE_INFO_WIDTH, imageInfoInResponse.getInt("width"));
                    imageInfo.addProperty(TSBImageMessageBody.IMAGE_INFO_HEIGHT, imageInfoInResponse.getInt("height"));
                    file.add(TSBImageMessageBody.IMAGE_INFO, imageInfo);
                    body.setFile(file);

                    message.setBody(body);
                    sendMessageRequest(message, callback);
                } catch (Exception e) {
                    LogUtil.error(LogUtil.LOG_TAG_CHAT, e);
                }
            }
        }, opt);
    }


    private void sendMediaMessage(final TSBMessage message, final TSBEngineCallback<TSBMessage> callback) {
        TSBEngineCallback<JSONObject> handlerCallback = getResponseHandlerOfMediaMessage(message, callback);
        if (!uploadMessageResourceToQiniu(message, handlerCallback)) {
            callback.onError(EngineConstants.CHANNEL_CODE_INVALID_OPERATION_ERROR, "Failed to get resource of the message.");
        }
    }

    private boolean uploadMessageResourceToQiniu(TSBMessage message, final TSBEngineCallback<JSONObject> responseHandler) {
        TSBMediaMessageBody mediaBody = (TSBMediaMessageBody)message.getBody();
        String filePath = mediaBody.getLocalPath();
        if (StrUtil.isEmpty(filePath)) {
            return false;
        }

        UploadManager manager = new UploadManager();
        String token = TSBChatManager.getInstance().getChatUser().getUploadToken();

        Map<String, String> params = new HashMap<String, String>();
        params.put("x:targetId", message.getRecipient());
        final UploadOptions opt = new UploadOptions(params, null, true, null, null);
        manager.put(filePath, null, token, new UpCompletionHandler() {

            @Override
            public void complete(String key, ResponseInfo info, JSONObject responseObject) {
                responseHandler.onSuccess(responseObject);
            }
        }, opt);
        return true;
    }

    private TSBEngineCallback<JSONObject> getResponseHandlerOfMediaMessage(final TSBMessage message, final TSBEngineCallback<TSBMessage> callback) {
        TSBEngineCallback<JSONObject> responseHandler = new TSBEngineCallback<JSONObject>() {

            @Override
            public void onSuccess(JSONObject responseObject) {
                try {

                    LogUtil.info(LogUtil.LOG_TAG_CHAT, "Get response from QINIU " + responseObject.toString(4));
                    TSBMediaMessageBody body = (TSBMediaMessageBody) message.getBody();

                    JsonObject file = new JsonObject();
                    file.addProperty(TSBImageMessageBody.KEY, responseObject.getString("key"));
                    file.addProperty(TSBImageMessageBody.ETAG, responseObject.getString("etag"));
                    file.addProperty(TSBImageMessageBody.NAME, responseObject.getString("fname"));
                    file.addProperty(TSBImageMessageBody.SIZE, responseObject.getString("fsize"));
                    file.addProperty(TSBImageMessageBody.MIME_TYPE, responseObject.getString("mimeType"));

                    TYPE messageType = body.getType();
                    if (messageType == TYPE.IMAGE) {
                        JSONObject imageInfoInResponse = responseObject.getJSONObject("imageInfo");
                        JsonObject imageInfo = new JsonObject();
                        imageInfo.addProperty(TSBImageMessageBody.IMAGE_INFO_WIDTH, imageInfoInResponse.getInt("width"));
                        imageInfo.addProperty(TSBImageMessageBody.IMAGE_INFO_HEIGHT, imageInfoInResponse.getInt("height"));
                        file.add(TSBImageMessageBody.IMAGE_INFO, imageInfo);
                        body.setFile(file);
                    } else if (messageType == TYPE.VOICE) {
                        JSONObject formatInfoInResponse = responseObject.getJSONObject("avinfo").getJSONObject("format");
                        JsonObject audioInfo = new JsonObject();
                        audioInfo.addProperty(TSBVoiceMessageBody.VOICE_INFO_DURATION, formatInfoInResponse.getString("duration"));
                        audioInfo.addProperty(TSBVoiceMessageBody.VOICE_INFO_FORMAT, formatInfoInResponse.getString("format_name"));
                        file.add(TSBVoiceMessageBody.VOICE_INFO, audioInfo);
                        body.setFile(file);
                    }

                    message.setBody(body);
                    sendMessageRequest(message, callback);
                } catch (Exception e) {
                    LogUtil.error(LogUtil.LOG_TAG_CHAT, e);
                }
            }

            @Override
            public void onError(int code, String message) {

            }
        };
        return responseHandler;
    }

    private void clearCache() {
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

    private String getTimestampString(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(date);
    }
}
