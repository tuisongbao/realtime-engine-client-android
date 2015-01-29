package com.tuisongbao.android.engine.engineio;

import java.net.URISyntaxException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter.Listener;
import com.github.nkzawa.engineio.client.Socket;
import com.github.nkzawa.engineio.client.transports.Polling;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.tuisongbao.android.engine.http.HttpConstants;
import com.tuisongbao.android.engine.http.request.BaseRequest;
import com.tuisongbao.android.engine.http.response.BaseResponse;
import com.tuisongbao.android.engine.service.EngineServiceListener;
import com.tuisongbao.android.engine.service.RawMessage;
import com.tuisongbao.android.engine.util.StrUtil;

public class EngineIOManager implements Runnable {

    private static final String TAG = EngineIOManager.class
            .getSimpleName();
    private static EngineIOManager mInstance;
    /**
     * 自增id
     */
    private Long mRequestId = 1L;
    private String mWebsocketHostUrl;
    private int mConnectionStatus = EngineConstants.CONNECTION_STATUS_NONE;
    private Socket mSocket;
    private Context mSDKAppContext;
    private RemoteCallbackList<EngineServiceListener> mRemoteCallbacks = new RemoteCallbackList<EngineServiceListener>(); 
    private ConcurrentMap<Long, ConcurrentHashMap<RawMessage, EngineServiceListener>> mCallbacks
            = new ConcurrentHashMap<Long, ConcurrentHashMap<RawMessage, EngineServiceListener>>();
    private BlockingDeque<String> mReceiveMessage = new LinkedBlockingDeque<String>();
    private boolean mIsRunning;

    public static EngineIOManager getInstance() {
        if (mInstance == null) {
            mInstance = new EngineIOManager();
        }
        return mInstance;
    }

    /**
     * 初始化Engine IO引擎
     * 
     * @param context
     */
    public synchronized void init(Context context) {
        if (!mIsRunning) {
            if (mSDKAppContext == null) {
                mSDKAppContext = context.getApplicationContext();
            }
            mIsRunning = true;
            new Thread(this).start();
        }
    }

    /**
     * 停止
     */
    public synchronized void stop() {
        if (mIsRunning) {
            mIsRunning = false;
            disconnect();
            mReceiveMessage.removeAll(null);
            mCallbacks.clear();
        }
    }

    @Override
    public void run() {
        while (mIsRunning) {
            if (StrUtil.isEmpty(getWebsocketURL())) {
                startConnect();
            } else if (mSocket == null) {
                openSocket();
            }
            startReceiveMessage();
        }
    }

    public boolean send(RawMessage msg) {
        return send(msg, null);
    }

    public boolean send(RawMessage msg, EngineServiceListener l) {
        if (isConnected()) {
            long requestId = getReqeustId();
            addEngineListener(requestId, msg, l);
            mSocket.send(toSendMessage(requestId, msg));
            return true;
        } else {
            return false;
        }
    }

    public void addEngineListener(long requestId, RawMessage msg, EngineServiceListener l) {
        if (msg != null && l != null) {
            ConcurrentHashMap<RawMessage, EngineServiceListener> map = new ConcurrentHashMap<RawMessage, EngineServiceListener>();
            map.put(msg, l);
            mCallbacks.put(requestId, map);
            registerListener(l);
        }
    }

    private void registerListener(EngineServiceListener l) {
        mRemoteCallbacks.register(l);
    }

    private void unregisterListener(EngineServiceListener l) {
        mRemoteCallbacks.unregister(l);
    }

    public boolean isConnected() {
        return mSocket != null && mConnectionStatus == EngineConstants.CONNECTION_STATUS_CONNECTED;
    }

    private long getReqeustId() {
        synchronized (mRequestId) {
            mRequestId++;
            return mRequestId;
        }
    }

    private String toSendMessage(long requestId, RawMessage msg) {
        JSONObject json = new JSONObject();
        try {
            json.put(EngineConstants.REQUEST_KEY_ID, requestId);
            json.put(EngineConstants.REQUEST_KEY_NAME, msg.getName());
            json.put(EngineConstants.REQUEST_KEY_DATA, new JSONObject(msg.getData()));
        } catch(Exception e) {
            showLog(e.getLocalizedMessage());
        }
        return json.toString();
    }

    private void startConnect() {
        BaseRequest request = new BaseRequest(HttpConstants.HTTP_METHOD_GET,
                HttpConstants.ENGINE_SERVER_REQUEST_URL
                        + "?ab3d5241778158b2864c0852" /*+ EngineConfig.instance().getAppId()*/);
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

    private void disconnect() {
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
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
                        showLog("Socket Message receive [msg=" + getArgsMSG(args) + "]");
                        mReceiveMessage.offer(args[0].toString());
                    }
                }).on(Socket.EVENT_ERROR, new Listener() {

                    @Override
                    public void call(Object... args) {
                        showLog("Socket Error [msg=" + ((Exception)args[0]).getLocalizedMessage());
                        mConnectionStatus = EngineConstants.CONNECTION_STATUS_ERROR;
                    }
                }).on(Socket.EVENT_CLOSE, new Listener() {

                    @Override
                    public void call(Object... args) {
                        showLog("Socket Close [msg=" + getArgsMSG(args) + "]");
                        mConnectionStatus = EngineConstants.CONNECTION_STATUS_CLOSED;
                        mSocket = null;
                    }
                }).on(Socket.EVENT_FLUSH, new Listener() {

                    @Override
                    public void call(Object... args) {
                        showLog("Socket Flush [msg=" + getArgsMSG(args) + "]");
                    }
                });
                mSocket.open();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void startReceiveMessage() {

        try {
            handleMessage(mReceiveMessage.take());
        } catch (InterruptedException e) {
            showLog(e.getLocalizedMessage());
        } catch (JSONException e) {
            showLog(e.getLocalizedMessage());
        }
    }

    private String getWebsocketURL() {
        if (StrUtil.isEmpty(mWebsocketHostUrl)) {
            return "";
        } else {
            return mWebsocketHostUrl
                    + "/engine.io/?transport=websocket&platform=Android&sdkVersion=v1.0.0&protocol=v1&appId=ab3d5241778158b2864c0852" /*+ EngineConfig.instance().getAppId()*/;
        }
    }

    private void showLog(String content) {
        Log.w(TAG, content);
    }

    private String getArgsMSG(Object... args) {
        return args == null
                || args.length == 0 ? "" : args.toString();
    }

    private void handleMessage(String msg) throws JSONException {
        showLog("received msg=" + msg);
        JSONObject json = new JSONObject(msg);
        // 获取请求类型
        String name = json.optString(EngineConstants.REQUEST_KEY_NAME);
        long requestId = StrUtil.toLong(json.optString(EngineConstants.REQUEST_KEY_RESPONSE_TO), 0);
        if (!StrUtil.isEmpty(name)) {
            // 当为链接状态时
            if (name.startsWith(EngineConstants.CONNECTION_PREFIX)) {
                int connectStatus = EngineConstants.getConnectionStatus(name);
                if (connectStatus == EngineConstants.CONNECTION_STATUS_ERROR) {
                    JSONObject data = json.optJSONObject(EngineConstants.REQUEST_KEY_DATA);
                    if (data != null) {
                        int code = data.optInt(EngineConstants.REQUEST_KEY_CODE);
                        // 4000 ~ 4099: 连接将被服务端关闭, 客户端 不 应该进行重连。
                        
                        // 4100 ~ 4199: 连接将被服务端关闭, 客户端应按照指示进行重连。
                        if (code >= 4100 && code <= 4199) {
                            mConnectionStatus = EngineConstants.CONNECTION_STATUS_DISCONNECTED;
                            disconnect();
                            openSocket();
                        }
                    }
                } else {
                    mConnectionStatus = connectStatus;
                }
            }
        } else {
            // empty
        }
        callback(requestId, msg);
    }
    
    private void callback(long requestId, String msg) {
        if (requestId > 0) {
            ConcurrentHashMap<RawMessage, EngineServiceListener> map = mCallbacks.remove(requestId);
            if (map != null && !map.isEmpty()) {
                RawMessage raw = map.keys().nextElement();
                raw.setData(msg);
                EngineServiceListener l = map.get(raw);
                if (l != null) {
                    mRemoteCallbacks.beginBroadcast();
                    try {
                        l.call(raw);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    mRemoteCallbacks.finishBroadcast();
                    unregisterListener(l);
                }
            }
        }
    }

    private EngineIOManager() {
        // empty
    }
}
