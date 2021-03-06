package com.tuisongbao.android.engine.engineio.interfaces;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.service.RawMessage;
import com.tuisongbao.android.engine.util.StrUtil;

public class EngineIoInterface extends BaseEngineIODataSource implements
        IEngineInterface {
    private static final String WSS_SCHEME = "wss";
    private String mWebsocketHostUrl;
    private int mConnectionStatus = EngineConstants.CONNECTION_STATUS_NONE;
    private Socket mSocket;
    private String mAppId;
    private String mSocketId;
    private EngineIoOptions mEngineIoOptions;
    /**
     * 重连次数间隔
     */
    private int mReconnectGap = 0;
    /**
     * 重连次数
     */
    private long mReconnectTimes = 0;
    /**
     * 重连策略
     */
    private String mReconnectStrategy = EngineConstants.CONNECTION_STRATEGY_BACKOFF;
    /**
     * 重连基数
     */
    private int mReconnectIn = EngineConstants.CONNECTION_STRATEGY_BACKOFF_DEFAULT_RECONNECTIN;
    /**
     * 重连最大间隔
     */
    private int mReconnectMax = EngineConstants.CONNECTION_STRATEGY_BACKOFF_DEFAULT_RECONNECTINMAX;
    private int mConnectionType = EngineConstants.CONNECTION_STRATEGY_CONNECTION_TYPE_RECONNECTION_BY_STRATEGY;

    public EngineIoInterface(IEngineCallback callback, EngineIoOptions options) {
        super(callback);
        mAppId = options.getAppId();
        mEngineIoOptions = options;
        start();
    }

    public EngineIoInterface( EngineIoOptions options) {
        this(null, options);
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
        showLog("Start to connection");
        preConnection();
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
        mWebsocketHostUrl = "";
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
                    LogUtil.error(LogUtil.LOG_TAG_ENGINEIO, "Engine server request url parse exception", e);
                }
                if (!StrUtil.isEmpty(mWebsocketHostUrl)) {
                    openSocket();
                }
            }
        }
    }

    /**
     * 该方法用于控制重连频率，在重连网络之前需要判断其需要经个多少再连一次
     */
    private void preConnection() {
        // 需要马上重连
        if (mConnectionType == EngineConstants.CONNECTION_STRATEGY_CONNECTION_TYPE_RECONNECTION_IMMEDIATELY
                && mReconnectTimes <= 0) {
            mReconnectTimes++;
            return;
        }
        if (EngineConstants.CONNECTION_STRATEGY_STATIC.equals(mReconnectStrategy)) {
            /**
             * static ：以静态的间隔进行重连，服务端可以通过 engine_connection:error Event 的
             * data.reconnectStrategy 来启用，通过 data.reconnectIn 设置重连间隔。
             */
            if (mReconnectIn <= 0) {
                mReconnectIn = EngineConstants.CONNECTION_STRATEGY_BACKOFF_DEFAULT_RECONNECTINMAX;
            } else {
                // empty
            }
            mReconnectGap = mReconnectIn;
        } else {
            /**
             * backoff ：默认策略，重连间隔从一个基数开始（默认为 0），每次乘以 2 ，直到达到最大值（默认为 10 秒）。服务端可以通过
             * engine_connection:error Event 的 data.reconnectIn 、 data.reconnectInMax
             * 来调整基数和最大值，当然对应的 data.reconnectStrategy 需为 backoff 。
             *
             * 以默认值为例，不断自动重连时，间隔将依次为（单位毫秒）：0 1 2 4 8 16 64 128 256 1024 2048 4096 8192
             * 10000 10000 ... 。
             */
            if (mReconnectMax <= 0) {
                mReconnectMax = EngineConstants.CONNECTION_STRATEGY_BACKOFF_DEFAULT_RECONNECTINMAX;
            }
            if (mReconnectIn < 0) {
                mReconnectMax = EngineConstants.CONNECTION_STRATEGY_BACKOFF_DEFAULT_RECONNECTIN;
            }
            if (mReconnectTimes <= 0) {
                mReconnectGap = mReconnectIn;
            } else {
                if (mReconnectGap <= 0) {
                    mReconnectGap = 1;
                } else if(mReconnectGap * 2 < mReconnectMax) {
                    mReconnectGap = mReconnectGap * 2;
                } else {
                    mReconnectGap = mReconnectMax;
                }
            }
        }
        try {
            showLog("Start to sleep： " + mReconnectGap);
            if (mReconnectGap > 0) {
                Thread.sleep(mReconnectGap);
            }
            showLog("end to sleep： " + mReconnectGap);
        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_ENGINEIO, "Connection sleep excetion", e);
        }
        mReconnectTimes++;
    }

    private void resetConnectionStrategy() {
        mReconnectGap = 0;
        mReconnectTimes = 0;
        mReconnectStrategy = EngineConstants.CONNECTION_STRATEGY_BACKOFF;
        mReconnectIn = EngineConstants.CONNECTION_STRATEGY_BACKOFF_DEFAULT_RECONNECTIN;
        mReconnectMax = EngineConstants.CONNECTION_STRATEGY_BACKOFF_DEFAULT_RECONNECTINMAX;
        mConnectionType = EngineConstants.CONNECTION_STRATEGY_CONNECTION_TYPE_RECONNECTION_BY_STRATEGY;
    }

    private void openSocket() {
        String url = getWebsocketURL();
        if (!StrUtil.isEmpty(url)) {
            try {
                Socket.Options ops = new Socket.Options();
                ops.transports = new String[] { WebSocket.NAME, Polling.NAME };
                if (url.toLowerCase().startsWith(WSS_SCHEME)) {
                    try {
                        SSLContext sslContext = createSSLContext();
                        ops.sslContext = sslContext;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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
                        showLog("Socket HandShake");
                        if (args != null && args.length > 0 && args[0] instanceof HandshakeData) {
                            mSocketId = ((HandshakeData) args[0]).sid;
                            showLog("Socket HandShake [mSocketId=" + mSocketId + "]");
                        }
                    }
                });
                mSocket.open();
                waitForReconnect();
            } catch (URISyntaxException e) {
                LogUtil.error(LogUtil.LOG_TAG_ENGINEIO, "Socket open exception", e);
            }
        }
    }

    private SSLContext createSSLContext() throws NoSuchAlgorithmException, KeyManagementException {

        TrustManager tm = new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
            }
        };

        SSLContext sslContext = null;
        sslContext = SSLContext.getInstance( "TLS" );

        sslContext.init(null, new TrustManager[] { tm }, null);
        return sslContext;
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

                if (EngineConstants.ENGINE_ENGINE_RESPONSE.equals(name)) {
                    // 说明是对客户端请求的response(engine_response)
                    requestId = StrUtil.toLong(
                            ret.optString(EngineConstants.REQUEST_KEY_RESPONSE_TO), 0);
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
                        LogUtil.error(LogUtil.LOG_TAG_ENGINEIO, "Parse response result exception", e);
                    }
                } else {
                    // 说明是事件
                    code = ret.optInt(EngineConstants.REQUEST_KEY_CODE);
                    if (code != EngineConstants.ENGINE_CODE_SUCCESS) {
                        errorMessage = ret
                                .optString(EngineConstants.REQUEST_KEY_ERROR_MESSAGE);
                    }
                    data = ret.toString();
                }
            } else {
                // Support sending un-json format event data
                data = json.optString(EngineConstants.REQUEST_KEY_DATA);
            }

            // 当为链接状态时
            if (name.startsWith(EngineConstants.CONNECTION_PREFIX)) {
                int connectStatus = EngineConstants.getConnectionStatus(name);
                if (connectStatus == EngineConstants.CONNECTION_STATUS_ERROR) {
                    if (code != EngineConstants.ENGINE_CODE_SUCCESS) {
                        // 4000 ~ 4099: 连接将被服务端关闭, 客户端 不 应该进行重连。
                        if (code >= 4000 && code <= 4099) {
                            mConnectionType = EngineConstants.CONNECTION_STRATEGY_CONNECTION_TYPE_FORBIDDEN_CONNECTION;
                            stop();
                        }
                        // 4100 ~ 4199: 连接将被服务端关闭, 客户端应按照指示进行重连。
                        if (code >= 4100 && code <= 4199) {
                            mConnectionType = EngineConstants.CONNECTION_STRATEGY_CONNECTION_TYPE_RECONNECTION_BY_STRATEGY;
                            setReconnectionStrategy(ret);
                            startReconnect(code, errorMessage);
                        }
                        // 4200 ~ 4299: 连接将被服务端关闭, 客户端应立即重连。
                        if (code >= 4200 && code <= 4299) {
                            mConnectionType = EngineConstants.CONNECTION_STRATEGY_CONNECTION_TYPE_RECONNECTION_IMMEDIATELY;
                            setReconnectionStrategy(ret);
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

    private void setReconnectionStrategy(JSONObject data) {
        String reconnectStrategy = data.optString(EngineConstants.REQUEST_KEY_RECONNECTION_STRATEGY);
        if (!StrUtil.isEmpty(reconnectStrategy)) {
            mReconnectStrategy = reconnectStrategy;
            int reconnectIn = data.optInt(EngineConstants.REQUEST_KEY_RECONNECTION_IN);
            if (reconnectIn >= 0) {
                mReconnectIn = reconnectIn;
            }
            int reconnectMax = data.optInt(EngineConstants.REQUEST_KEY_RECONNECTION_INMAX);
            if (reconnectMax >= 0) {
                mReconnectMax = reconnectMax;
            }
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
                LogUtil.error(LogUtil.LOG_TAG_ENGINEIO, "handle sent failed message exception", e);
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
            LogUtil.error(LogUtil.LOG_TAG_ENGINEIO, "handle disconnected message exception", e);
        }
        handleMessage(rawMessage);
    }

    private void handleConnectedMessage() {
        resetConnectionStrategy();
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
        LogUtil.info(LogUtil.LOG_TAG_ENGINEIO, StrUtil.strNotNull(content));
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
                LogUtil.error(LogUtil.LOG_TAG_ENGINEIO, "Prepare websocket url", e);
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
