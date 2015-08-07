package com.tuisongbao.engine.connection;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.HandshakeData;
import com.github.nkzawa.engineio.client.Socket;
import com.github.nkzawa.engineio.client.transports.Polling;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.google.gson.Gson;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.connection.entity.ConnectionEventData;
import com.tuisongbao.engine.engineio.source.BaseEngineIODataSource;
import com.tuisongbao.engine.http.HttpConstants;
import com.tuisongbao.engine.http.request.BaseRequest;
import com.tuisongbao.engine.http.response.BaseResponse;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.utils.StrUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class Connection extends BaseEngineIODataSource {
    public static final String EVENT_STATE_CHANGED = "state_changed";
    public static final String EVENT_CONNECTING = "connecting";
    public static final String EVENT_CONNECT_IN = "connecting_in";
    public static final String EVENT_ERROR = "error";

    public enum State {
        Initialized("initialized"),
        Connecting("connecting"),
        Connected("connected"),
        Disconnected("disconnected");

        private String name;

        State(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

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

    private static final String TAG = "TSB" + Connection.class.getSimpleName();
    /***
     * Record the last error received from server. If socket closed unexpectedly, use the strategy in this error to reconnect.
     */
    protected ConnectionEventData lastConnectionError;
    protected Socket mSocket;
    protected Engine mEngine;
    protected State mLastState;

    private Long mRequestId = 1L;
    private Options mOptions = new Options();

    public Connection(Engine engine) {
        mEngine = engine;
        mOptions.setAppId(mEngine.getEngineOptions().getAppId());
        mLastState = State.Initialized;

        start();
    }

    public boolean isConnected() {
        return mLastState == State.Connected;
    }

    public String getSocketId() {
        return mSocket.id();
    }

    @Override
    public void connect() {
        LogUtil.info(TAG, "Connecting...");

        if (mLastState != State.Connecting) {
            trigger(EVENT_CONNECTING);
            mLastState = State.Connecting;
        }

        String appId = mEngine.getEngineOptions().getAppId();
        BaseRequest request = new BaseRequest(HttpConstants.HTTP_METHOD_GET, HttpConstants.ENGINE_SERVER_REQUEST_URL
                        + "?" + appId);
        BaseResponse response = request.execute();
        if (response != null && response.isStatusOk()) {
            JSONObject json = response.getJSONData();
            if (json != null) {
                String socketAddr = "";
                try {
                    socketAddr = json.getString(Protocol.REQUEST_KEY_WS_ADDR);
                } catch (JSONException e) {
                    LogUtil.error(TAG, "Engine server request url getCallbackData exception", e);
                }
                String webSocketUrl = getWebSocketURL(socketAddr);
                if (!StrUtils.isEmpty(webSocketUrl)) {
                    openSocket(webSocketUrl);
                    return;
                }
            }
        }
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
        trigger(EVENT_STATE_CHANGED, mLastState, state);

        mLastState = state;
    }

    protected void onSocketClosed(Object... args) {
        LogUtil.info(TAG, "Socket Close [msg=" + getArgsMSG(args) + "]");
        updateState(State.Disconnected);
    }

    public BaseEvent send(BaseEvent event) {
        event.setId(getRequestId());
        String eventString = event.serialize();
        LogUtil.verbose(TAG, "Send rawEvent:" + eventString);
        mSocket.send(eventString);
        return event;
    }

    private String getWebSocketURL(String socketAddr) {
        if (StrUtils.isEmpty(socketAddr)) {
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
                    LogUtil.verbose(TAG, "Socket Message receive " + args[0].toString());
                    if (args != null && args.length > 0) {
                        try {
                            onEvent(args[0].toString());
                        } catch (Exception e) {
                            LogUtil.info(TAG, "Handle Message Exception [msg="
                                    + e.getLocalizedMessage() + "]");
                            trigger(EVENT_ERROR);
                        }
                    }
                }
            }).on(Socket.EVENT_ERROR, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    LogUtil.error(TAG, "Socket Error [msg="
                            + ((Exception) args[0]).getLocalizedMessage());
                }
            }).on(Socket.EVENT_CLOSE, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    onSocketClosed(args);
                }
            }).on(Socket.EVENT_FLUSH, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    LogUtil.verbose(TAG, "Socket Flush [msg=" + getArgsMSG(args) + "]");
                }
            }).on(Socket.EVENT_TRANSPORT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    LogUtil.verbose(TAG, "Socket Transport [msg=" + getArgsMSG(args) + "]");
                }
            }).on(Socket.EVENT_HANDSHAKE, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    LogUtil.verbose(TAG, "Socket HandShake");
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

    protected void handleConnectionEvent(String eventName, ConnectionEventData data) {
        if (StrUtils.isEqual(eventName, Protocol.EVENT_NAME_CONNECTION_ERROR)) {
            LogUtil.info(TAG, "Connection error: " + data);
            lastConnectionError = data;
            trigger(EVENT_ERROR);
            disconnect();
        } else if (StrUtils.isEqual(eventName, Protocol.EVENT_NAME_CONNECTION_ESTABLISHED)) {
            LogUtil.info(TAG, "Connected");
            updateState(State.Connected);
        } else {
            LogUtil.info(TAG, "Unknown event " + eventName + " with message " + data.getMessage());
        }
    }

    private void onEvent(String eventString) throws JSONException {
        Gson gson = new Gson();
        RawEvent rawEvent = gson.fromJson(eventString, RawEvent.class);
        String eventName = rawEvent.getName();
        if (Protocol.isConnectionEvent(eventName)) {
            ConnectionEventData connectionData = gson.fromJson(rawEvent.getData(), ConnectionEventData.class);
            handleConnectionEvent(eventName, connectionData);
        } else if (Protocol.isValidEvent(eventName)) {
            // Notify the pipeline which will ferries data to sink
            dispatchEvent(eventString);
        } else {
            LogUtil.warn(TAG, "Received unknown event");
        }
    }

    private String getArgsMSG(Object... args) {
        return args == null || args.length == 0 ? "<empty>" : args.toString();
    }
}
