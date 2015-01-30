package com.tuisongbao.android.engine.engineio.interfaces;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.github.nkzawa.engineio.client.HandshakeData;
import com.github.nkzawa.engineio.client.Socket;
import com.github.nkzawa.engineio.client.transports.Polling;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.engineio.EngineIoOptions;
import com.tuisongbao.android.engine.engineio.exception.DataSourceException;
import com.tuisongbao.android.engine.engineio.source.BaseEngineIODataSource;
import com.tuisongbao.android.engine.engineio.source.IEngineCallback;
import com.tuisongbao.android.engine.http.HttpConstants;
import com.tuisongbao.android.engine.http.request.BaseRequest;
import com.tuisongbao.android.engine.http.response.BaseResponse;
import com.tuisongbao.android.engine.service.RawMessage;
import com.tuisongbao.android.engine.util.StrUtil;

public class EngineIoInterface extends BaseEngineIODataSource implements
        IEngineInterface {
    private final static String TAG = EngineIoInterface.class.getSimpleName();
    private String mWebsocketHostUrl;
    private int mConnectionStatus = EngineConstants.CONNECTION_STATUS_NONE;
    private Socket mSocket;
    private String mAppId;
    private String mSocketId;
    private EngineIoOptions mEngineIoOptions;

    public EngineIoInterface(IEngineCallback callback, Context context,
            EngineIoOptions options) {
        super(callback, context);
        mAppId = options.getAppId();
        mEngineIoOptions = options;
        start();
    }

    public EngineIoInterface(Context context, EngineIoOptions options) {
        this(null, context, options);
    }

    @Override
    public boolean receive(RawMessage message) {
        if (isConnected()) {
            String msg = toSendMessage(message);
            showLog("Send message: " + msg);
            mSocket.send(msg);
            return true;
        } else {
            handleSentFailedMessage(message);
            showLog("Send message failed");
            return false;
        }
    }

    @Override
    public boolean setResource(String resource) {
        mAppId = resource;
        return true;
    }

    @Override
    public boolean isConnected() {
        return mSocket != null
                && mConnectionStatus == EngineConstants.CONNECTION_STATUS_CONNECTED;
    }
    
    public String getSocketId() {
        return mSocketId;
    }

    @Override
    protected void waitForConnection() throws DataSourceException,
            InterruptedException {
        if (StrUtil.isEmpty(getWebsocketURL())) {
            startConnect();
        } else if (mSocket == null) {
            openSocket();
        }
    }

    @Override
    protected void disconnect() {
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }
    }

    private void startConnect() {
        BaseRequest request = new BaseRequest(HttpConstants.HTTP_METHOD_GET,
                HttpConstants.ENGINE_SERVER_REQUEST_URL
                        + "?" + mAppId);
        BaseResponse response = request.execute();
        if (response != null && response.isStatusOk()) {
            JSONObject json = response.getJSONData();
            if (json != null) {
                try {
                    mWebsocketHostUrl = json.getString("addr");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!StrUtil.isEmpty(mWebsocketHostUrl)) {
                    openSocket();
                }
            }
        }
    }

    private void openSocket() {
        String url = getWebsocketURL();
        if (!StrUtil.isEmpty(url)) {
            try {
                Socket.Options ops = new Socket.Options();
                ops.transports = new String[] { WebSocket.NAME, Polling.NAME };
                mSocket = new Socket(url, ops);
                mSocket.on(Socket.EVENT_OPEN, new Listener() {

                    @Override
                    public void call(Object... args) {
                        showLog("Socket Open [msg=" + getArgsMSG(args) + "]");
                    }
                }).on(Socket.EVENT_MESSAGE, new Listener() {

                    @Override
                    public void call(Object... args) {
                        showLog("Socket Message receive [msg="
                                + getArgsMSG(args) + "]");
                        if (args != null && args.length > 0) {
                            try {
                                handleMessage(args[0].toString());
                            } catch (JSONException e) {
                                showLog("Handle Message Exception [msg="
                                        + e.getLocalizedMessage() + "]");
                            }
                        }
                    }
                }).on(Socket.EVENT_ERROR, new Listener() {

                    @Override
                    public void call(Object... args) {
                        showLog("Socket Error [msg="
                                + ((Exception) args[0]).getLocalizedMessage());
//                        startReconnect(
//                                EngineConstants.CONNECTION_CODE_CONNECTION_EXCEPTION,
//                                "Connection closed [exception="
//                                        + ((Exception) args[0])
//                                                .getLocalizedMessage() + "]");
                    }
                }).on(Socket.EVENT_CLOSE, new Listener() {

                    @Override
                    public void call(Object... args) {
                        showLog("Socket Close [msg=" + getArgsMSG(args) + "]");
                        startReconnect(EngineConstants.CONNECTION_CODE_CONNECTION_CLOSED, "Connection closed");
                    }
                }).on(Socket.EVENT_FLUSH, new Listener() {

                    @Override
                    public void call(Object... args) {
                        showLog("Socket Flush [msg=" + getArgsMSG(args) + "]");
                    }
                }).on(Socket.EVENT_TRANSPORT, new Listener() {

                    @Override
                    public void call(Object... args) {
                        showLog("Socket Transport [msg=" + getArgsMSG(args) + "]");
                    }
                }).on(Socket.EVENT_HANDSHAKE, new Listener() {

                    @Override
                    public void call(Object... args) {
                        showLog("Socket HandShake [msg=" + getArgsMSG(args) + "]");
                        if (args != null && args.length > 0 && args[0] instanceof HandshakeData) {
                            mSocketId = ((HandshakeData) args[0]).sid;
                            showLog("Socket HandShake [mSocketId=" + mSocketId + "]");
                        }
                    }
                });
                mSocket.open();
                connected();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(String msg) throws JSONException {
        showLog("received msg=" + msg);
        JSONObject json = new JSONObject(msg);
        // 获取请求类型
        String name = json.optString(EngineConstants.REQUEST_KEY_NAME);
        // request id 只有在非事件中使用
        long requestId = 0;
        // 用于客户端回复服务段
        long serverRequestId = json.optLong(EngineConstants.REQUEST_KEY_ID);
        String channel = json.optString(EngineConstants.REQUEST_KEY_CHANNEL);
        if (!StrUtil.isEmpty(name)) {
            JSONObject ret = json
                    .optJSONObject(EngineConstants.REQUEST_KEY_DATA);
            String data = null;
            int code = EngineConstants.ENGINE_CODE_SUCCESS;
            String errorMessage = "";
            if (ret != null) {
                requestId = StrUtil.toLong(
                        ret.optString(EngineConstants.REQUEST_KEY_RESPONSE_TO), 0);
                if (requestId > 0) {
                    // 说明是对客户端请求的response
                    boolean ok = ret
                            .optBoolean(EngineConstants.REQUEST_KEY_RESPONSE_OK);
                    try {
                        if (ok) {
                            // result 的数据及可能是 json object 也可能是 json array
                            JSONObject result = ret
                                    .optJSONObject(EngineConstants.REQUEST_KEY_RESPONSE_RESULT);
                            if (result == null) {
                                JSONArray arrayResult = ret.optJSONArray(EngineConstants.REQUEST_KEY_RESPONSE_RESULT);
                                if (arrayResult != null) {
                                    data = arrayResult.toString();
                                }
                            } else {
                                data = result.toString();
                            }
                        } else {
                            JSONObject error = ret.getJSONObject(EngineConstants.REQUEST_KEY_RESPONSE_ERROR);
                            if (error != null) {
                                code = error.optInt(EngineConstants.REQUEST_KEY_CODE);
                                errorMessage = error
                                        .optString(EngineConstants.REQUEST_KEY_ERROR_MESSAGE);
                            } else {
                                code = EngineConstants.ENGINE_CODE_UNKNOWN;
                                errorMessage = "Unknow error";
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // 说明是事件
                    code = ret.optInt(EngineConstants.REQUEST_KEY_CODE);
                    if (code != EngineConstants.ENGINE_CODE_SUCCESS) {
                        errorMessage = ret
                                .optString(EngineConstants.REQUEST_KEY_ERROR_MESSAGE);
                    }
                }
            }
            // 当为链接状态时
            if (name.startsWith(EngineConstants.CONNECTION_PREFIX)) {
                int connectStatus = EngineConstants.getConnectionStatus(name);
                if (connectStatus == EngineConstants.CONNECTION_STATUS_ERROR) {
                    if (code != EngineConstants.ENGINE_CODE_SUCCESS) {
                        // 4000 ~ 4099: 连接将被服务端关闭, 客户端 不 应该进行重连。
                        if (code >= 4000 && code <= 4099) {
                            // empty
                        }
                        // 4100 ~ 4199: 连接将被服务端关闭, 客户端应按照指示进行重连。
                        if (code >= 4100 && code <= 4199) {
                            startReconnect(code, errorMessage);
                        }
                    }
                } else {
                    handleConnectedMessage();
                }
            }
            RawMessage rawMessage = new RawMessage(mAppId, mAppId, name, data);
            rawMessage.setChannel(channel);
            rawMessage.setRequestId(requestId);
            rawMessage.setCode(code);
            rawMessage.setErrorMessage(errorMessage);
            rawMessage.setServerRequestId(serverRequestId);
            handleMessage(rawMessage);
        } else {
            // empty
        }
    }
    
    private void handleSentFailedMessage(RawMessage message) {
        message.setCode(EngineConstants.CONNECTION_CODE_CONNECTION_SEND_MESSAGE_FAILED);
        message.setErrorMessage("Send message failed");
        // handle channel event
        if (StrUtil.strNotNull(message.getName()).startsWith(
                EngineConstants.CHANNEL_NAME_PREFIX)
                && StrUtil.isEmpty(message.getData())) {
            try {
                JSONObject json = new JSONObject(message.getData());
                String channel = json.optString(EngineConstants.REQUEST_KEY_CHANNEL);
                if (!StrUtil.isEmpty(channel)) {
                    message.setBindName(channel);
                }
                message.setName(EngineConstants.CONNECTION_NAME_CONNECTION_SUCCEEDED_ERROR);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        handleMessage(message);
    }

    private void startReconnect(int code, String messsage) {
        disconnect();
        if (mConnectionStatus == EngineConstants.CONNECTION_STATUS_CONNECTED) {
            mConnectionStatus = EngineConstants.CONNECTION_STATUS_CONNECTING;
            handleDisconnectedMessage(code, messsage);
        }
        reconnect();
    }

    private void handleDisconnectedMessage(int code, String messsage) {
        RawMessage rawMessage = genConnectionBindRawMessage(EngineConstants.CONNECTION_NAME_CONNECTION_SUCCEEDED_ERROR, messsage);
        rawMessage.setCode(code);
        rawMessage.setErrorMessage(messsage);
        JSONObject json = new JSONObject();
        try {
            json.put(EngineConstants.REQUEST_KEY_CODE, code);
            json.put(EngineConstants.REQUEST_KEY_ERROR_MESSAGE, StrUtil.strNotNull(messsage));
            rawMessage.setData(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        handleMessage(rawMessage);
    }

    private void handleConnectedMessage() {
        if (mConnectionStatus != EngineConstants.CONNECTION_STATUS_CONNECTED) {
            mConnectionStatus = EngineConstants.CONNECTION_STATUS_CONNECTED;
            RawMessage rawMessage = genConnectionBindRawMessage(EngineConstants.CONNECTION_NAME_CONNECTION_SUCCEEDED, "success");
            rawMessage.setCode(EngineConstants.ENGINE_CODE_SUCCESS);
            handleMessage(rawMessage);
        }
    }

    private RawMessage genConnectionBindRawMessage(String name, String data) {
        RawMessage rawMessage = new RawMessage(mAppId, mAppId, name, data);
        rawMessage.setBindName(EngineConstants.EVENT_CONNECTION_CHANGE_STATUS);
        return rawMessage;
    }

    private void showLog(String content) {
        Log.w(TAG, StrUtil.strNotNull(content));
    }

    private String getArgsMSG(Object... args) {
        return args == null || args.length == 0 ? "<empty>" : args.toString();
    }

    private String getWebsocketURL() {
        if (StrUtil.isEmpty(mWebsocketHostUrl)) {
            return "";
        } else {
            try {
                return mWebsocketHostUrl
                        + "/engine.io/?transport="
                        + mEngineIoOptions.getTransport()
                        + "&platform="
                        + URLEncoder.encode(mEngineIoOptions.getPlatform().replace(" ", "%20"),
                                "UTF-8")
                        + "&sdkVersion="
                        + mEngineIoOptions.getSDKVersion()
                        + "&protocol="
                        + mEngineIoOptions.getProtocol() + "&appId="
                        + mAppId;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            };
            return "";
        }
    }

    private String toSendMessage(RawMessage msg) {
        JSONObject json = new JSONObject();
        try {
            json.put(EngineConstants.REQUEST_KEY_ID, msg.getRequestId());
            json.put(EngineConstants.REQUEST_KEY_NAME, msg.getName());
            json.put(EngineConstants.REQUEST_KEY_DATA,
                    new JSONObject(msg.getData()));
        } catch (Exception e) {
            showLog(e.getLocalizedMessage());
        }
        return json.toString();
    }

}
