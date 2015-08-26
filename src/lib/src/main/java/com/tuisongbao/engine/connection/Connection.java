package com.tuisongbao.engine.connection;

import com.github.nkzawa.engineio.client.HandshakeData;
import com.github.nkzawa.engineio.client.Socket;
import com.github.nkzawa.engineio.client.transports.Polling;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.google.gson.Gson;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.engineio.source.BaseEngineIODataSource;
import com.tuisongbao.engine.http.HttpConstants;
import com.tuisongbao.engine.http.BaseRequest;
import com.tuisongbao.engine.http.BaseResponse;
import com.tuisongbao.engine.utils.LogUtils;
import com.tuisongbao.engine.utils.PushUtils;
import com.tuisongbao.engine.utils.StrUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/**
 * <STRONG>网络连接管理类</STRONG>
 *
 * <P>
 *     每个 Engine 都有一个该实例，用于建立 WebSocket 连接和底层消息的收发，包括 Chat 和 Pub/Sub。
 *     该类提供连接状态的通知，可以通过 {@link #bind(State, Listener)} 方法绑定 {@link State} 状态的通知。
 *     需注意的是，当连接断开时，该类 <STRONG>不会</STRONG> 再重新连接，其子类 {@link AutoReconnectConnection} 会进行自动重连。还可以绑定的事件有
 *
 * <UL>
 *     <LI>{@value #EVENT_CONNECTING_IN}</LI>
 *     <LI>{@value #EVENT_STATE_CHANGED}</LI>
 *     <LI>{@value #EVENT_ERROR}</LI>
 * </UL>
 *
 * @author Katherine Zhu
 */
public class Connection extends BaseEngineIODataSource {
    /**
     * 连接状态发生转换时会触发该事件，事件回调接收两个参数，类型均为 {@link State}:
     *
     * <pre>
     *    connection.bind(Connection.EVENT_STATE_CHANGED, new Emitter.Listener() {
     *        &#64;Override
     *        public void call(final Object... args) {
     *            Log.i(TAG, "连接状态从 " + args[0] + " 转变为 " + args[1]);
     *        }
     *    });
     * </pre>
     */
    public static final String EVENT_STATE_CHANGED = "state_changed";
    /**
     * 准备重新建立连接时会触发该事件，事件回调接收一个参数，类型为 {@code int}, 单位为 <STRONG>秒</STRONG>:
     *
     * <pre>
     *    connection.bind(Connection.EVENT_STATE_CHANGED, new Emitter.Listener() {
     *        &#64;Override
     *        public void call(final Object... args) {
     *            Log.i(TAG, "将在 " + args[0] + " 秒后重连");
     *        }
     *    });
     * </pre>
     */
    public static final String EVENT_CONNECTING_IN = "connecting_in";
    /**
     * 连接发生错误时会触发该事件，事件回调接收一个参数，类型为 {@code String}:
     *
     * <pre>
     *    connection.bind(Connection.EVENT_STATE_CHANGED, new Emitter.Listener() {
     *        &#64;Override
     *        public void call(final Object... args) {
     *            Log.i(TAG, "连接出错，错误原因：" + args[0]);
     *        }
     *    });
     * </pre>
     */
    public static final String EVENT_ERROR = "error";

    /**
     * Connection 帮助类，表连接状态，可通过绑定事件处理方法获取状态通知
     *
     */
    public enum State {
        /**
         * 初始化
         */
        Initialized("initialized"),
        /**
         * 连接中，
         */
        Connecting("connecting"),
        /**
         * 已连接
         */
        Connected("connected"),
        /**
         * 连接失败，配置错误引起的，可通过绑定 {@link #EVENT_ERROR} 事件获取具体错误信息
         */
        Failed("failed"),
        /**
         * 连接断开，之前连接过，现在被<STRONG>主动断开</STRONG>时会有该状态
         */
        Disconnected("disconnected");

        private final String name;

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

    private class Options {
        private String appId;
        private String platform = "Android";
        private String protocol = "v1";
        // TODO: Replace sdkVersion automatically by gradle task.
        private String sdkVersion = "v2.1.0";
        private String transport = "websocket";

        public Options() {
        }

        public void setAppId(String appId) {
            this.appId = appId;
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
    ConnectionEventData lastConnectionError;
    private Socket mSocket;
    private final Engine mEngine;
    protected State mLastState;

    private Long mRequestId = 1L;
    private final Options mOptions = new Options();

    Connection(Engine engine) {
        mEngine = engine;
        mOptions.setAppId(mEngine.getEngineOptions().getAppId());
        updateState(State.Initialized);

        start();
    }

    public boolean isConnected() {
        return mLastState == State.Connected;
    }

    public String getSocketId() {
        return mSocket.id();
    }

    /**
     * 建立连接，可通过绑定 {@link State} 获取连接结果
     *
     * <P>
     * 如果当前状态为 {@link State#Connected}，不会做任何操作。建议在调用 {@link #disconnect()} 之后，用来恢复连接时使用。
     */
    @Override
    public void connect() {
        LogUtils.info(TAG, "Connecting...");

        if (mLastState != State.Connecting) {
            updateState(State.Connecting);
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
                    LogUtils.error(TAG, "Engine server request url getCallbackData exception", e);
                }
                String webSocketUrl = getWebSocketURL(socketAddr);
                if (!StrUtils.isEmpty(webSocketUrl)) {
                    openSocket(webSocketUrl);
                }
            }
        }
    }

    /**
     * 断开连接，可通过绑定 {@link State} 获取连接结果
     *
     * <P>
     * 如果当前状态为 {@link State#Disconnected}，不会做任何操作。连接断开后，可通过调用 {@link #connect()} 恢复连接。
     */
    @Override
    public void disconnect() {
        if (mLastState == State.Disconnected) {
            LogUtils.warn(TAG, "Already disconnected");
            return;
        }
        if (mSocket != null) {
            mSocket.close();
            updateState(State.Disconnected);
            mSocket = null;
        }
    }

    /**
     * 绑定连接状态回调处理方法
     *
     * <pre>
     *    connection.bind(Connection.State.Connected, new Emitter.Listener() {
     *        &#64;Override
     *        public void call(final Object... args) {
     *            Connection.State state = (Connection.State)args[0];
     *            Log.i(TAG, "Connection state: " + state.toString());
     *        }
     *    });
     * </pre>
     *
     * @param state     连接状态
     * @param listener  处理方法，回调中包含一个参数，表明当前状态，类型为 {@code State}
     */
    public void bind(State state, Listener listener) {
        bind(state.getName(), listener);
    }

    /**
     * 解绑指定处理方法
     *
     * @param state     连接状态
     * @param listener  处理方法
     */
    public void unbind(State state, Listener listener) {
        unbind(state.getName(), listener);
    }

    void updateState(State state) {
        if (state == mLastState) return;

        trigger(state.getName(), state);

        LogUtils.info(TAG, "State changed from " + mLastState + " to " + state);
        trigger(EVENT_STATE_CHANGED, mLastState, state);

        mLastState = state;
    }

    void onSocketClosed(Object... args) {
        LogUtils.info(TAG, "Socket Close [msg=" + getArgsMSG(args) + "]");
    }

    public BaseEvent send(BaseEvent event) {
        event.setId(getRequestId());
        String eventString = event.serialize();
        LogUtils.verbose(TAG, "Send event:" + eventString);
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
            LogUtils.warn(TAG, "Encoding Platform: " + mOptions.getPlatform() + " failed by " + e.getMessage());
            webSocketUrl += "Unknown";
        }
        return webSocketUrl;
    }

    private void openSocket(String url) {
        try {
            Socket.Options ops = new Socket.Options();
            ops.transports = new String[] { WebSocket.NAME, Polling.NAME };

            mSocket = new Socket(url, ops);
            mSocket.on(Socket.EVENT_OPEN, new Listener() {

                @Override
                public void call(Object... args) {
                    LogUtils.info(TAG, "Socket Open [msg=" + getArgsMSG(args) + "]");
                }
            }).on(Socket.EVENT_MESSAGE, new Listener() {

                @Override
                public void call(Object... args) {
                    LogUtils.verbose(TAG, "Socket receive " + args[0].toString());
                    if (args.length > 0) {
                        try {
                            onEvent(args[0].toString());
                        } catch (Exception e) {
                            LogUtils.info(TAG, "Handle Message Exception [msg="
                                    + e.getLocalizedMessage() + "]");
                        }
                    }
                }
            }).on(Socket.EVENT_ERROR, new Listener() {

                @Override
                public void call(Object... args) {
                    String errorMessage = ((Exception) args[0]).getLocalizedMessage();
                    LogUtils.error(TAG, "Socket Error [msg=" + errorMessage);
                    trigger(EVENT_ERROR, errorMessage);
                }
            }).on(Socket.EVENT_CLOSE, new Listener() {

                @Override
                public void call(Object... args) {
                    onSocketClosed(args);
                }
            }).on(Socket.EVENT_FLUSH, new Listener() {

                @Override
                public void call(Object... args) {
                    LogUtils.verbose(TAG, "Socket Flush [msg=" + getArgsMSG(args) + "]");
                }
            }).on(Socket.EVENT_TRANSPORT, new Listener() {

                @Override
                public void call(Object... args) {
                    LogUtils.verbose(TAG, "Socket Transport [msg=" + getArgsMSG(args) + "]");
                }
            }).on(Socket.EVENT_HANDSHAKE, new Listener() {

                @Override
                public void call(Object... args) {
                    LogUtils.verbose(TAG, "Socket HandShake");
                    if (args != null && args.length > 0 && args[0] instanceof HandshakeData) {
                        LogUtils.info(TAG, "Socket HandShake [mSocketId=" + ((HandshakeData) args[0]).sid + "]");
                    }
                }
            });
            mSocket.open();
            waitForConnect();
        } catch (URISyntaxException e) {
            LogUtils.error(TAG, "Open socket failed", e);
        }
    }

    private long getRequestId() {
        synchronized (mRequestId) {
            mRequestId++;
            return mRequestId;
        }
    }

    void handleConnectionEvent(String eventName, ConnectionEventData data) {
        if (StrUtils.isEqual(eventName, Protocol.EVENT_NAME_CONNECTION_ERROR)) {
            LogUtils.info(TAG, "Connection error: " + data);
            lastConnectionError = data;
            updateState(State.Failed);
            trigger(EVENT_ERROR, data.getMessage());
            disconnect();
        } else if (StrUtils.isEqual(eventName, Protocol.EVENT_NAME_CONNECTION_ESTABLISHED)) {
            LogUtils.info(TAG, "Connected");
            updateState(State.Connected);

            PushUtils.bindPush(mEngine);
        } else {
            LogUtils.info(TAG, "Unknown event " + eventName + " with message " + data.getMessage());
        }
    }

    private void onEvent(String eventString) {
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
            LogUtils.warn(TAG, "Received unknown event");
        }
    }

    private String getArgsMSG(Object... args) {
        if (args.length > 0) {
            return args[0].toString();
        } else {
            return "empty content";
        }
    }
}
