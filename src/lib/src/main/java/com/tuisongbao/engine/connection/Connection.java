package com.tuisongbao.engine.connection;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.HandshakeData;
import com.github.nkzawa.engineio.client.Socket;
import com.github.nkzawa.engineio.client.transports.Polling;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.common.ITSBResponseMessage;
import com.tuisongbao.engine.engineio.DataPipeline;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.engineio.sink.TSBListenerSink;
import com.tuisongbao.engine.engineio.source.BaseEngineIODataSource;
import com.tuisongbao.engine.http.HttpConstants;
import com.tuisongbao.engine.http.request.BaseRequest;
import com.tuisongbao.engine.http.response.BaseResponse;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.service.RawMessage;
import com.tuisongbao.engine.util.StrUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

// TODO: Connection events, "state_changed", "connecting_in", "connecting", "error"
public class Connection extends BaseEngineIODataSource {

    public class Options {
        private String appId;
        private String platform = "Android";
        private String protocol = "v1";
        // TODO: Replace sdkVersion automatically by gradle task.
        private String sdkVersion = "v1.0.0";
        private String transport = "websocket";

        public Options() {
        }

        public Options setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public Options setPlatform(String platform) {
            this.platform = platform;
            return this;
        }

        public Options setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Options setSDKVersion(String sdkVersion) {
            this.sdkVersion = sdkVersion;
            return this;
        }

        public Options setTransport(String transport) {
            this.transport = transport;
            return this;
        }

        public String getAppId() {
            return appId;
        }

        public String getPlatform() {
            return platform;
        }

        public String getProtocol() {
            return protocol;
        }

        public String getSdkVersion() {
            return sdkVersion;
        }

        public String getTransport() {
            return transport;
        }
    }

    enum State {
        Initialized("initialized"),
        Connected("connected"),
        Disconnected("disconnected");

        private String name;

        State(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final String TAG = Connection.class.getSimpleName();
    /***
     * Record the last error received from server. If socket closed unexceptedly, use the strategy in this error to reconnect.
     */
    protected JSONObject lastConnectionError;
    protected Socket mSocket;
    protected TSBEngine mEngine;
    protected boolean mConnected;
    protected State mLastState;

    private DataPipeline mDataPipeline = new DataPipeline();
    private TSBListenerSink mNotifier = new TSBListenerSink();
    private Long mRequestId = 1L;
    private Options mOptions = new Options();

    public Connection(TSBEngine engine) {
        mEngine = engine;
        mOptions.setAppId(mEngine.getEngineOptions().getAppId());
        mDataPipeline.addSource(this);
        mLastState = State.Initialized;

        start();
    }

    public boolean ismConnected() {
        return mConnected;
    }

    public String getSocketId() {
        return mSocket.id();
    }

    public void connect() {
        LogUtil.info(TAG, "Connecting...");
        String appId = mEngine.getEngineOptions().getAppId();
        BaseRequest request = new BaseRequest(HttpConstants.HTTP_METHOD_GET,
                HttpConstants.ENGINE_SERVER_REQUEST_URL
                        + "?" + appId);
        BaseResponse response = request.execute();
        if (response != null && response.isStatusOk()) {
            JSONObject json = response.getJSONData();
            if (json != null) {
                String socketAddr = "";
                try {
                    socketAddr = json.getString(Protocol.REQUEST_KEY_WS_ADDR);
                } catch (JSONException e) {
                    LogUtil.error(TAG, "Engine server request url parse exception", e);
                }
                String webSocketUrl = getWebSocketURL(socketAddr);
                if (!StrUtil.isEmpty(webSocketUrl)) {
                    openSocket(webSocketUrl);
                    return;
                }
            }
        }
        // TODO: Handle connect failed
    }

    @Override
    public void disconnect() {
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }
    }

    protected void updateState(State state) {
        if (state == mLastState) return;

        LogUtil.info(TAG, "State changed from " + mLastState + " to " + state);
        mLastState = state;
    }

    protected void onSocketClosed(Object... args) {
        LogUtil.info(TAG, "Socket Close [msg=" + getArgsMSG(args) + "]");
        updateState(State.Disconnected);
    }

    public void send(String name, String data, ITSBResponseMessage response) throws JSONException {
        RawMessage message = new RawMessage(name, data);
        message.setRequestId(getRequestId());
        if (response != null) {
            mNotifier.register(message, response);
        }
        mSocket.send(message.serialize());
    }

    public void bind(String name, ITSBResponseMessage response) {
        if (response != null && !StrUtil.isEmpty(name)) {
            mNotifier.bind(name, response);
        }
    }

    public void unbind(String name, ITSBResponseMessage response) {
        if (response != null) {
            mNotifier.unbind(name, response);
        } else {
            mNotifier.unbind(name);
        }
    }

    private String getWebSocketURL(String socketAddr) {
        if (StrUtil.isEmpty(socketAddr)) {
            return "";
        }
        String webSocketUrl = socketAddr
                + "/engine.io/?transport="
                + mOptions.getTransport()
                + "&sdkVersion="
                + mOptions.getSdkVersion()
                + "&protocol="
                + mOptions.getProtocol()
                + "&appId="
                + mOptions.getAppId()
                + "&platform=";
        try {
            webSocketUrl += URLEncoder.encode(mOptions.getPlatform().replace(" ", "%20"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LogUtil.warn(TAG, "Encoding Platform: " + mOptions.getPlatform() + " failed by " + e.getMessage());
            webSocketUrl += "Unknown";
        }
        return webSocketUrl;
    }

    private void openSocket(String url) {
        try {
            Socket.Options ops = new Socket.Options();
            ops.transports = new String[] { WebSocket.NAME, Polling.NAME };

            mSocket = new Socket(url, ops);
            mSocket.on(Socket.EVENT_OPEN, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    LogUtil.info(TAG, "Socket Open [msg=" + getArgsMSG(args) + "]");
                }
            }).on(Socket.EVENT_MESSAGE, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    LogUtil.info(TAG, "Socket Message receive [msg=" + getArgsMSG(args) + "]");
                    if (args != null && args.length > 0) {
                        try {
                            handleEvent(args[0].toString());
                        } catch (JSONException e) {
                            LogUtil.info(TAG, "Handle Message Exception [msg="
                                    + e.getLocalizedMessage() + "]");
                        }
                    }
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    LogUtil.info(TAG, "Socket Error [msg="
                            + ((Exception) args[0]).getLocalizedMessage());
                }
            }).on(Socket.EVENT_CLOSE, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    // TODO: Can only bind one listener on each state
                    onSocketClosed(args);
                }
            }).on(Socket.EVENT_FLUSH, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    LogUtil.info(TAG, "Socket Flush [msg=" + getArgsMSG(args) + "]");
                }
            }).on(Socket.EVENT_TRANSPORT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    LogUtil.info(TAG, "Socket Transport [msg=" + getArgsMSG(args) + "]");
                }
            }).on(Socket.EVENT_HANDSHAKE, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    LogUtil.info(TAG, "Socket HandShake");
                    if (args != null && args.length > 0 && args[0] instanceof HandshakeData) {
                        LogUtil.info(TAG, "Socket HandShake [mSocketId=" + ((HandshakeData) args[0]).sid + "]");
                    }
                }
            });
            mSocket.open();
            waitForConnect();
        } catch (URISyntaxException e) {
            LogUtil.error(TAG, "Open socket failed", e);
        }
    }

    private long getRequestId() {
        synchronized (mRequestId) {
            mRequestId++;
            return mRequestId;
        }
    }

    protected void handleConnectionEvent(String eventName, JSONObject data) {
        if (StrUtil.isEqual(eventName, Protocol.EVENT_NAME_CONNECTION_ERROR)) {
            LogUtil.info(TAG, "Connection error: " + data);
            lastConnectionError = data;
            mConnected = false;
            disconnect();
        } else if (StrUtil.isEqual(eventName, Protocol.EVENT_NAME_CONNECTION_ESTABLISHED)) {
            // TODO: Notify listeners
            LogUtil.info(TAG, "Connected");
            mConnected = true;
            updateState(State.Connected);
        }
    }

    private void handleChannelEvent(String eventName, JSONObject data) {
        // TODO:
    }

    private void handleEvent(String eventString) throws JSONException {
        JSONObject event = Protocol.parseEvent(eventString);
        String eventName = event.optString(Protocol.REQUEST_KEY_NAME);
        JSONObject data = event.optJSONObject(Protocol.REQUEST_KEY_DATA);
        if (Protocol.isChannelEvent(eventName)) {
            handleChannelEvent(eventName, data);
        } else if (Protocol.isConnectionEvent(eventName)) {
            handleConnectionEvent(eventName, data);
        } else if (Protocol.isServerResponseEvent(eventName)) {

        }
    }

    private String getArgsMSG(Object... args) {
        return args == null || args.length == 0 ? "<empty>" : args.toString();
    }
}
