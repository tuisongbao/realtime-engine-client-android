package com.tuisongbao.android.engine.chat;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.entity.ChatType;
import com.tuisongbao.android.engine.chat.entity.TSBChatLoginData;
import com.tuisongbao.android.engine.chat.entity.TSBChatMessageSendData;
import com.tuisongbao.android.engine.chat.entity.TSBChatUser;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.entity.TSBMessageBody;
import com.tuisongbao.android.engine.chat.message.TSBChatLoginMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatLoginResponseMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatLogoutMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatMessageGetMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatMessageResponseMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatMessageResponseMessage.TSBChatMessageResponseMessageCallback;
import com.tuisongbao.android.engine.chat.message.TSBChatMessageSendMessage;
import com.tuisongbao.android.engine.chat.serializer.TSBChatMessageBodySerializer;
import com.tuisongbao.android.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.android.engine.chat.serializer.TSBChatMessageTypeSerializer;
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
import com.tuisongbao.android.engine.service.EngineServiceManager;
import com.tuisongbao.android.engine.util.ExecutorUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBChatManager extends BaseManager {

    private TSBChatUser mTSBChatUser;
    private TSBChatLoginMessage mTSBLoginMessage;

    private static TSBChatManager mInstance;

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

    public boolean isLogin() {
        return mTSBChatUser != null;
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


    /**
     * 发送消息
     *
     * @param message
     *            消息
     * @param callback
     */
    public void sendMessage(TSBMessage message,
            TSBEngineCallback<TSBMessage> callback) {
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
            if (!isLogin()) {
                // 初次登陆时需要回调
                mTSBChatUser = t;
                if (mTSBLoginMessage != null && mTSBLoginMessage.getCallback() != null) {
                    mTSBLoginMessage.getCallback().onSuccess(t);
                }
            } else {
                // 由于网络原因等重新登陆时不需要需要回调
                mTSBChatUser = t;
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
                        response.getCustomCallback().onSuccess(message);
                    }
                } else if (EngineConstants.CHAT_NAME_NEW_MESSAGE
                        .equals(response.getBindName())) {
                    // 接收到消息
                    if (response.getData() != null) {
                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.registerTypeAdapter(ChatType.class,
                                new TSBChatMessageChatTypeSerializer());
                        gsonBuilder.registerTypeAdapter(TSBMessage.TYPE.class,
                                new TSBChatMessageTypeSerializer());
                        gsonBuilder.registerTypeAdapter(TSBMessageBody.class,
                                new TSBChatMessageBodySerializer());
                        Gson gson = gsonBuilder.create();
                        TSBMessage message = gson.fromJson(response.getData(),
                                TSBMessage.class);
                        EngineServiceManager.receivedMessage(message);
                        // response to server
                        long serverRequestId = response.getServerRequestId();
                        TSBEngineResponseToServerRequestMessage request = new TSBEngineResponseToServerRequestMessage(
                                serverRequestId, true);
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
}
