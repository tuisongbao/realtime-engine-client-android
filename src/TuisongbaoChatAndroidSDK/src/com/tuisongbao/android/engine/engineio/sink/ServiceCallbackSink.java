package com.tuisongbao.android.engine.engineio.sink;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.tuisongbao.android.engine.service.EngineServiceListener;
import com.tuisongbao.android.engine.service.RawMessage;
import com.tuisongbao.android.engine.util.StrUtil;

/**
 * A data sink that sends new message through an AIDL interface.
 * 
 */
public class ServiceCallbackSink extends BaseEngineCallbackSink {

    /**
     * 自增request id
     */
    private Long mRequestId = 1L;
    private int mListenerCount;
    private ConcurrentMap<String, ConcurrentHashMap<String, EngineServiceListener>> mBinds = new ConcurrentHashMap<String, ConcurrentHashMap<String, EngineServiceListener>>();
    private ConcurrentMap<Long, ConcurrentHashMap<RawMessage, EngineServiceListener>> mCallbacks = new ConcurrentHashMap<Long, ConcurrentHashMap<RawMessage, EngineServiceListener>>();
    private RemoteCallbackList<EngineServiceListener> mListeners = new RemoteCallbackList<EngineServiceListener>();

    /**
     * Returns request id
     * 
     * @param listener
     * @return
     */
    public long register(RawMessage message, EngineServiceListener listener) {
        synchronized (mListeners) {
            long requestId = getReqeustId();
            if (message != null && listener != null) {
                mListeners.register(listener);
                ConcurrentHashMap<RawMessage, EngineServiceListener> map = new ConcurrentHashMap<RawMessage, EngineServiceListener>();
                message.setRequestId(requestId);
                map.put(message, listener);
                mCallbacks.put(requestId, map);
            }
            return requestId;
        }
    }

    public void unregister(RawMessage message, EngineServiceListener listener) {
        synchronized (mListeners) {
            mListeners.unregister(listener);
            mCallbacks.remove(message.getRequestId());
        }
    }

    /**
     * Bind event
     * 
     * @param listener
     * @return
     */
    public synchronized void bind(RawMessage message, EngineServiceListener listener) {
        synchronized (mListeners) {
            if (message != null && listener != null) {
                ConcurrentHashMap<String, EngineServiceListener> map = mBinds.get(message.getBindName());
                if (map == null) {
                    map = new ConcurrentHashMap<String, EngineServiceListener>();
                }
                map.put(message.getAppId(), listener);
            }
        }
    }

    /**
     * Unbind event
     * 
     * @param message
     * @param listener 解绑的回调函数
     */
    public void unbind(RawMessage message, EngineServiceListener listener) {
        synchronized (mListeners) {
            if (message != null) {
                // TODO： need call back unbind message
                ConcurrentHashMap<String, EngineServiceListener> map = mBinds.get(message.getBindName());
                if (map != null) {
                    map.remove(message.getAppId());
                    if (listener != null) {
                        mListeners.register(listener);
                        mListeners.beginBroadcast();
                        try {
                            listener.call(message);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        mListeners.finishBroadcast();
                        mListeners.unregister(listener);
                    }
                }
            }
        }
    }

    public int getListenerCount() {
        return mListenerCount;
    }

    @Override
    protected void propagateMessage(RawMessage rawMessage) {
        synchronized (mListeners) {
            // 处理回调事件
            long requestId = rawMessage.getRequestId();
            ConcurrentHashMap<RawMessage, EngineServiceListener> map = mCallbacks.get(requestId);
            if (map != null && !map.isEmpty()) {
                RawMessage raw = map.keys().nextElement();
                raw.setChannel(rawMessage.getChannel());
                raw.setData(rawMessage.getData());
                raw.setCode(rawMessage.getCode());
                raw.setErrorMessge(rawMessage.getErrorMessge());
                EngineServiceListener listener = map.get(raw);
                if (listener != null) {
                    mListeners.beginBroadcast();
                    try {
                        listener.call(raw);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    mListeners.finishBroadcast();
                    unregister(rawMessage, listener);
                }
            }
        }
        // 处理绑定事件
        // channel绑定事件处理
        if (!StrUtil.isEmpty(rawMessage.getChannel())) {
            rawMessage.setBindName(rawMessage.getChannel());
            rawMessage.setIsUnbind(true);
            callbackBindListener(rawMessage);
        }
        // name绑定事件处理
        if (!StrUtil.isEmpty(rawMessage.getName())) {
            rawMessage.setBindName(rawMessage.getName());
            rawMessage.setIsUnbind(true);
            callbackBindListener(rawMessage);
        }
    }

    private void callbackBindListener(RawMessage rawMessage) {
        ConcurrentHashMap<String, EngineServiceListener> map = mBinds.get(rawMessage.getBindName());
        if (map != null) {
            EngineServiceListener listener = map.get(rawMessage.getAppId());
            if (listener != null) {
                mListeners.beginBroadcast();
                try {
                    listener.call(rawMessage);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    map.remove(rawMessage.getAppId());
                }
                mListeners.finishBroadcast();
            }
        }
    }

    /**
     * 获取request id
     * 
     * @return
     */
    private long getReqeustId() {
        synchronized (mRequestId) {
            mRequestId++;
            return mRequestId;
        }
    }
};
