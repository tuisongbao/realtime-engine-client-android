package com.tuisongbao.engine.connection;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.HandshakeData;
import com.github.nkzawa.engineio.client.Socket;
import com.github.nkzawa.engineio.client.transports.Polling;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.google.gson.Gson;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.common.Event;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.TSBEngineBindCallback;
import com.tuisongbao.engine.connection.entity.ConnectionEventData;
import com.tuisongbao.engine.engineio.source.BaseEngineIODataSource;
import com.tuisongbao.engine.http.HttpConstants;
import com.tuisongbao.engine.http.request.BaseRequest;
import com.tuisongbao.engine.http.response.BaseResponse;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.StrUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// TODO: 15-7-31 Use EventEmitter to handle listeners
public class Connection extends BaseEngineIODataSource {
    public enum ConnectionEvent {
        StateChanged("state_changed"),
        ConnectingIn("connecting_in"),
        Connecting("connecting"),
        Error("error");

        private String name;

        ConnectionEvent(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
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

    private static final String TAG = Connection.class.getSimpleName();
    /***
     * Record the last error received from server. If socket closed unexpectedly, use the strategy in this error to reconnect.
     */
    protected ConnectionEventData lastConnectionError;
    protected Socket mSocket;
    protected TSBEngine mEngine;
    protected State mLastState;
    protected ConcurrentMap<String, Set<TSBEngineBindCallback>> mEventListeners = new ConcurrentHashMap<>();

    private Long mRequestId = 1L;
    private Options mOptions = new Options();

    public Connection(TSBEngine engine) {
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

    public void connect() {
        LogUtil.info(TAG, "Connecting...");

        callbackListeners(ConnectionEvent.Connecting);

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

    /***
     * Bind sink to the connectionEvent, when connectionEvent takes place, sink will be called. Support multiple listeners binding to a connectionEvent, but
     * if you bind multiple same listeners to a connectionEvent, sink only be called once.
     *
     * @param connectionEvent
     * @param callback
     * @return
     */
    public boolean bind(ConnectionEvent connectionEvent, TSBEngineBindCallback callback) {
        try {
            Set<TSBEngineBindCallback> listeners = mEventListeners.get(connectionEvent.name);
            synchronized (listeners) {
                if (listeners == null) {
                    listeners = new HashSet<>();
                }
                listeners.add(callback);
                mEventListeners.put(connectionEvent.name, listeners);
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(TAG, e);
            return false;
        }
    }

    /***
     * Unbind sink from a connectionEvent.
     *
     * @param connectionEvent
     * @param callback Optional, Can be null, which means remove all listeners from the connectionEvent
     * @return
     */
    public boolean unbind(ConnectionEvent connectionEvent, TSBEngineBindCallback callback) {
        // TODO: 15-7-31 Test this API
        try {
            Set<TSBEngineBindCallback> listeners = mEventListeners.get(connectionEvent.name);
            if (listeners == null) {
                return true;
            }
            if (callback == null) {
                mEventListeners.remove(connectionEvent.name);
                return true;
            }
            synchronized (listeners) {
                listeners.remove(callback);
                mEventListeners.put(connectionEvent.name, listeners);
                return true;
            }
        } catch (Exception e) {
            LogUtil.error(TAG, e);
            return false;
        }
    }

    protected void callbackListeners(ConnectionEvent connectionEvent, Object... args) {
        Set<TSBEngineBindCallback> listeners = mEventListeners.get(connectionEvent.name);
        if (listeners == null) {
            return;
        }
        synchronized (listeners) {
            for (TSBEngineBindCallback listener: listeners) {
                listener.onEvent(connectionEvent.name, args);
            }
        }
    }

    protected void updateState(State state) {
        if (state == mLastState) return;

        LogUtil.info(TAG, "State changed from " + mLastState + " to " + state);
        callbackListeners(ConnectionEvent.StateChanged, mLastState, state);

        mLastState = state;
    }

    protected void onSocketClosed(Object... args) {
        LogUtil.info(TAG, "Socket Close [msg=" + getArgsMSG(args) + "]");
        updateState(State.Disconnected);
    }

    public Event send(String name, String data) throws JSONException {
        Event event = new Event(name, data);
        event.setId(getRequestId());
        mSocket.send(event.serialize());
        return event;
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
                    LogUtil.info(TAG, "Socket Message receive");
                    if (args != null && args.length > 0) {
                        try {
                            LogUtil.info(TAG, "Got event:" + args[0].toString());
                            onEvent(args[0].toString());
                        } catch (JSONException e) {
                            LogUtil.info(TAG, "Handle Message Exception [msg="
                                    + e.getLocalizedMessage() + "]");
                            callbackListeners(ConnectionEvent.Error);
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
                    // TODO: Can only bind one sink on each state
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

    protected void handleConnectionEvent(String eventName, ConnectionEventData data) {
        if (StrUtil.isEqual(eventName, Protocol.EVENT_NAME_CONNECTION_ERROR)) {
            LogUtil.info(TAG, "Connection error: " + data);
            lastConnectionError = data;
            callbackListeners(ConnectionEvent.Error);
            disconnect();
        } else if (StrUtil.isEqual(eventName, Protocol.EVENT_NAME_CONNECTION_ESTABLISHED)) {
            // TODO: Notify listeners
            LogUtil.info(TAG, "Connected");
            updateState(State.Connected);
        }
    }

    private void onEvent(String eventString) throws JSONException {
        Gson gson = new Gson();
        com.tuisongbao.engine.common.Event event = gson.fromJson(eventString, com.tuisongbao.engine.common.Event.class);
        String eventName = event.getName();
        if (Protocol.isConnectionEvent(eventName)) {
            ConnectionEventData connectionData = gson.fromJson(event.getData(), ConnectionEventData.class);
            handleConnectionEvent(eventName, connectionData);
        } else if (Protocol.isServerResponseEvent(eventName)) {
            // Notify the pipeline which will ferries data to sink
            dispatchEvent(event);
        }
    }

    private String getArgsMSG(Object... args) {
        return args == null || args.length == 0 ? "<empty>" : args.toString();
    }
}
