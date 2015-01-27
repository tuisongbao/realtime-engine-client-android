package com.tuisongbao.android.engine.engineio.interfaces;

import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.github.nkzawa.engineio.client.Socket;
import com.github.nkzawa.engineio.client.transports.Polling;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.engineio.exception.DataSinkException;
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

    public EngineIoInterface(IEngineCallback callback, Context context,
            String appId) {
        super(callback, context);
        setResource(appId);
        start();
    }

    public EngineIoInterface(Context context, String appId) {
        this(null, context, appId);
    }

    @Override
    public boolean receive(RawMessage message) throws DataSinkException {
        if (isConnected()) {
            mSocket.send(toSendMessage(message));
            return true;
        } else {
            // TODO feed back error
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

    @Override
    protected void waitForConnection() throws DataSourceException,
            InterruptedException {
        if (StrUtil.isEmpty(getWebsocketURL())) {
            startConnect();
        } else if (mSocket == null) {
            openSocket();
        }

        // waiting for receive message
        mConnectionChanged.await();
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
        if (response.isStatusOk()) {
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
                        // TODO: need parse error condition
                        mConnectionStatus = EngineConstants.CONNECTION_STATUS_ERROR;
                    }
                }).on(Socket.EVENT_CLOSE, new Listener() {

                    @Override
                    public void call(Object... args) {
                        showLog("Socket Close [msg=" + getArgsMSG(args) + "]");
                        // TODO: need parse error condition
                        mConnectionStatus = EngineConstants.CONNECTION_STATUS_CLOSED;
                        disconnect();
                        reconnect();
                    }
                }).on(Socket.EVENT_FLUSH, new Listener() {

                    @Override
                    public void call(Object... args) {
                        showLog("Socket Flush [msg=" + getArgsMSG(args) + "]");
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
        long requestId = StrUtil.toLong(
                json.optString(EngineConstants.REQUEST_KEY_ID), 0);
        String channel = json.optString(EngineConstants.REQUEST_KEY_CHANNEL);
        if (!StrUtil.isEmpty(name)) {
            JSONObject data = json
                    .optJSONObject(EngineConstants.REQUEST_KEY_DATA);
            int code = EngineConstants.ERROR_CODE_SUCCESS;
            String errorMessage = "";
            if (data != null) {
                code = json.optInt(EngineConstants.REQUEST_KEY_CODE);
                if (code != EngineConstants.ERROR_CODE_SUCCESS) {
                    errorMessage = json
                            .optString(EngineConstants.REQUEST_KEY_ERROR_MESSAGE);
                }
            }
            // 当为链接状态时
            if (name.startsWith(EngineConstants.CONNECTION_PREFIX)) {
                int connectStatus = EngineConstants.getConnectionStatus(name);
                if (connectStatus == EngineConstants.CONNECTION_STATUS_ERROR) {
                    if (code != EngineConstants.ERROR_CODE_SUCCESS) {
                        // 4000 ~ 4099: 连接将被服务端关闭, 客户端 不 应该进行重连。
                        if (code >= 4000 && code <= 4099) {
                            // TODO: need do something
                        }
                        // 4100 ~ 4199: 连接将被服务端关闭, 客户端应按照指示进行重连。
                        if (code >= 4100 && code <= 4199) {
                            mConnectionStatus = EngineConstants.CONNECTION_STATUS_DISCONNECTED;
                            // TODO: need do something
                            disconnect();
                            reconnect();
                        }
                    }
                } else {
                    mConnectionStatus = connectStatus;
                }
            }
            RawMessage rawMessage = new RawMessage(mAppId, name, data != null ? data.toString(): "");
            rawMessage.setChannel(channel);
            rawMessage.setRequestId(requestId);
            rawMessage.setCode(code);
            rawMessage.setErrorMessge(errorMessage);
            handleMessage(rawMessage);
        } else {
            // empty
        }
    }

    private void showLog(String content) {
        Log.w(TAG, content);
    }

    private String getArgsMSG(Object... args) {
        return args == null || args.length == 0 ? "" : args.toString();
    }

    private String getWebsocketURL() {
        if (StrUtil.isEmpty(mWebsocketHostUrl)) {
            return "";
        } else {
            return mWebsocketHostUrl
                    + "/engine.io/?transport=websocket&platform=Android&sdkVersion=v1.0.0&protocol=v1&appId=" + mAppId;
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
