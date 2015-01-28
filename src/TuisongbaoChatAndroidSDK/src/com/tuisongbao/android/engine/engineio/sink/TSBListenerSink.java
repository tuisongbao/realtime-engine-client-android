package com.tuisongbao.android.engine.engineio.sink;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.tuisongbao.android.engine.common.ITSBEngineCallback;
import com.tuisongbao.android.engine.common.ITSBResponseMessage;
import com.tuisongbao.android.engine.service.RawMessage;
import com.tuisongbao.android.engine.util.StrUtil;

/**
 * A data sink that sends new messages of specific types to listeners.
 * 
 */
public class TSBListenerSink extends BaseEngineCallbackSink {

    private ConcurrentMap<String, ConcurrentMap<RawMessage, ITSBResponseMessage>> mListeners = new ConcurrentHashMap<String, ConcurrentMap<RawMessage, ITSBResponseMessage>>();
    private ConcurrentMap<String, ConcurrentMap<ITSBEngineCallback, ITSBResponseMessage>> mBinds = new ConcurrentHashMap<String, ConcurrentMap<ITSBEngineCallback, ITSBResponseMessage>>();

    public TSBListenerSink() {
        // emtpy
    }

    public void register(RawMessage message, ITSBResponseMessage callBack) {
        ConcurrentMap<RawMessage, ITSBResponseMessage> map = new ConcurrentHashMap<RawMessage, ITSBResponseMessage>();
        map.put(message, callBack);
        mListeners.put(message.getUUID(), map);
    }

    public void unregister(RawMessage message) {
        mListeners.remove(message.getUUID());
    }

    public void bind(String name, ITSBResponseMessage response) {
        if (response != null) {
            ITSBEngineCallback callback = response.getCallback();
            if (callback != null) {
                ConcurrentMap<ITSBEngineCallback, ITSBResponseMessage> map = mBinds.get(name);
                if (map == null) {
                    map = new ConcurrentHashMap<ITSBEngineCallback, ITSBResponseMessage>();
                }
                map.put(callback, response);
                mBinds.put(name, map);
            }
        }
    }

    public void unbind(String name) {
        mBinds.remove(name);
    }

    public void unbind(String name, ITSBResponseMessage response) {
        if (response != null) {
            ITSBEngineCallback callback = response.getCallback();
            if (callback != null) {
                ConcurrentMap<ITSBEngineCallback, ITSBResponseMessage> map = mBinds.get(name);
                if (map != null) {
                    map.remove(callback);
                }
            }
        }
    }

    @Override
    protected void propagateMessage(RawMessage message) {
        if (message != null) {
            // 处理回调事件
            ConcurrentMap<RawMessage, ITSBResponseMessage> map = mListeners
                    .remove(message.getUUID());
            if (map != null) {
                ITSBResponseMessage callbackMessage = map.get(map.keySet()
                        .iterator().next());
                if (callbackMessage != null) {
                    callbackMessage.setCode(message.getCode());
                    callbackMessage.setErrorMessage(message.getErrorMessge());
                    callbackMessage.setChannel(message.getChannel());
                    callbackMessage.setData(message.getData());
                    callbackMessage.setName(message.getName());
                    callbackMessage.setData(message.getData());
                    callbackMessage.setBindName(message.getBindName());
                    callbackMessage.callBack();
                }
            }
            // 处理绑定事件
            // service绑定事件处理(该类事件为service本身产生的事件，而非引擎返回事件)
            if (!StrUtil.isEmpty(message.getBindName())) {
                callbackBindListener(message);
            }
            // channel绑定事件处理
            if (!StrUtil.isEmpty(message.getChannel())) {
                message.setBindName(message.getChannel());
                callbackBindListener(message);
            }
            // name绑定事件处理
            if (!StrUtil.isEmpty(message.getName())) {
                message.setBindName(message.getName());
                callbackBindListener(message);
            }
//            if (!StrUtil.isEmpty(message.getBindName())) {
//                ITSBResponseMessage callbackMessage = mBinds.get(message.getBindName());
//                if (message.isUnbind()) {
//                    unbind(message.getBindName());
//                }
//                if (callbackMessage != null) {
//                    callbackMessage.setCode(message.getCode());
//                    callbackMessage.setErrorMessage(message.getErrorMessge());
//                    callbackMessage.setChannel(message.getChannel());
//                    callbackMessage.setData(message.getData());
//                    callbackMessage.setName(message.getName());
//                    callbackMessage.setData(message.getData());
//                    callbackMessage.setBindName(message.getBindName());
//                    callbackMessage.callBack();
//                }
//            }
        }
    }
    
    private void callbackBindListener(RawMessage message) {
        ConcurrentMap<ITSBEngineCallback, ITSBResponseMessage> map = mBinds.get(message.getBindName());
        if (map != null && !map.isEmpty()) {
            Iterator<ITSBResponseMessage> iterator = map.values().iterator();
            while (iterator.hasNext()) {
                ITSBResponseMessage callbackMessage = iterator.next();
                if (callbackMessage != null) {
                    callbackMessage.setCode(message.getCode());
                    callbackMessage.setErrorMessage(message.getErrorMessge());
                    callbackMessage.setChannel(message.getChannel());
                    callbackMessage.setData(message.getData());
                    callbackMessage.setName(message.getName());
                    callbackMessage.setData(message.getData());
                    callbackMessage.setBindName(message.getBindName());
                    callbackMessage.callBack();
                }
            }
        }
    }
}
