package com.tuisongbao.android.engine.chat;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.tuisongbao.android.engine.EngineConfig;
import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.entity.TSBChatLoginData;
import com.tuisongbao.android.engine.chat.entity.TSBChatUser;
import com.tuisongbao.android.engine.chat.message.TSBChatLoginMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatLoginResponseMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatLogoutMessage;
import com.tuisongbao.android.engine.common.BaseManager;
import com.tuisongbao.android.engine.common.TSBEngineBindCallback;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.connection.entity.TSBConnection;
import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.entity.TSBEngineConstants;
import com.tuisongbao.android.engine.http.HttpConstants;
import com.tuisongbao.android.engine.http.request.BaseRequest;
import com.tuisongbao.android.engine.http.response.BaseResponse;
import com.tuisongbao.android.engine.util.ExecutorUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBChatManager extends BaseManager {

    private TSBChatLoginData mTSBChatLoginData;
    private TSBChatLoginMessage mTSBLoginMessage;

    private static TSBChatManager mInstance;

    public static TSBChatManager getInstance() {
        if (mInstance == null) {
            mInstance = new TSBChatManager();
        }
        return mInstance;
    }

    private TSBChatManager() {
        super();
    }

    /**
     * 聊天登陆
     * 
     * @param userData
     * @param callback
     */
    public void login(String userData, TSBEngineCallback<TSBChatUser> callback) {
        if (isLogin()) {
            handleErrorMessage(mTSBLoginMessage,
                    TSBEngineConstants.CHAT_CODE_LOGIN_HAS_LOGINED,
                    "user has been logined");
            return;
        }
        if (TSBEngine.isConnected()) {
            TSBChatLoginMessage message = new TSBChatLoginMessage();
            TSBChatLoginData data = new TSBChatLoginData();
            data.setUserData(userData);
            message.setData(data);
            bind(TSBChatLoginMessage.NAME, mLoginEngineCallback);
            auth(message);
            mTSBLoginMessage = message;
        } else {
            callback.onError(
                    TSBEngineConstants.CONNECTION_CODE_CONNECTION_SEND_MESSAGE_FAILED,
                    "can't connect to engine server");
        }
    }

    /**
     * 聊天登陆
     * 
     * @param userData
     * @param callback
     */
    public void logout(TSBEngineCallback<String> callback) {
        if (!isLogin()) {
            callback.onSuccess("logout success");
            logout();
            return;
        }
        if (TSBEngine.isConnected()) {
            TSBChatLogoutMessage message = new TSBChatLogoutMessage();
            message.setCallback(callback);
            bind(TSBChatLoginMessage.NAME, mLoginEngineCallback);
        } else {
            callback.onError(
                    TSBEngineConstants.CONNECTION_CODE_CONNECTION_SEND_MESSAGE_FAILED,
                    "can't connect to engine server");
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

    private void logout() {
        mTSBChatLoginData = null;
        mTSBLoginMessage = null;
    }

    private void handleErrorMessage(TSBChatLoginMessage msg, int code,
            String message) {
        msg.getCallback().onError(code, message);
    }

    private boolean isLogin() {
        return mTSBChatLoginData != null;
    }

    private void auth(final TSBChatLoginMessage msg) {
        ExecutorUtil.getThreadQueue().execute(new Runnable() {

            @Override
            public void run() {
                TSBChatLoginData data = msg.getData();
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
                        HttpConstants.HTTP_METHOD_POST, EngineConfig.instance()
                                .getAuthEndpoint(), json.toString());
                BaseResponse response = request.execute();
                if (response != null && response.isStatusOk()) {
                    JSONObject jsonData = response.getJSONData();
                    if (jsonData == null) {
                        // feed back empty
                        handleErrorMessage(msg,
                                TSBEngineConstants.CHAT_CODE_LOGIN_FAILED,
                                "auth failed, feed back auth data is empty");
                    } else {
                        String signature = jsonData.optString("signature");
                        if (StrUtil.isEmpty(signature)) {
                            // signature data empty
                            handleErrorMessage(msg,
                                    TSBEngineConstants.CHAT_CODE_LOGIN_FAILED,
                                    "auth failed, signature is empty");
                        } else {
                            data.setSignature(signature);
                        }
                        String userData = jsonData.optString("userData");
                        data.setUserData(userData);
                        send(msg, new TSBChatLoginResponseMessage());
                    }
                } else {
                    // connection to user server error or user server feed back
                    // error
                    handleErrorMessage(msg,
                            TSBEngineConstants.CHAT_CODE_LOGIN_FAILED,
                            "auth failed, connection to user server error or user server feed back error");
                }
            }
        });
    }

    private TSBEngineBindCallback mLoginEngineCallback = new TSBEngineBindCallback() {

        @Override
        public void onEvent(String bindName, String name, String data) {
            // login
            if (TSBChatLoginMessage.NAME.equals(bindName)) {
                mTSBChatLoginData = new Gson().fromJson(data,
                        TSBChatLoginData.class);
            } else if (TSBChatLogoutMessage.NAME.equals(bindName)) {
                try {
                    JSONObject result = new JSONObject(data);
                    int code = result.optInt(EngineConstants.REQUEST_KEY_CODE);
                    if (EngineConstants.ENGINE_CODE_SUCCESS == code) {
                        logout();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void handleConnect(TSBConnection t) {
        // when logined, it need to re-login
        if (isLogin() && mTSBLoginMessage != null) {
            auth(mTSBLoginMessage);
        }
    }

    @Override
    protected void handleDisconnect(int code, String message) {
        // empty
    }
}
