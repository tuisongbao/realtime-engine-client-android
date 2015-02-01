package com.tuisongbao.android.engine.engineio.sink;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.tuisongbao.android.engine.common.ITSBEngineCallback;
import com.tuisongbao.android.engine.common.ITSBResponseMessage;
import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.service.RawMessage;
import com.tuisongbao.android.engine.util.StrUtil;

/**
 * A data sink that sends new messages of specific types to listeners.
 * 
 */
public class TSBListenerSink extends BaseEngineCallbackSink {

    private ConcurrentMap<Long, ConcurrentMap<RawMessage, ITSBResponseMessage>> mListeners = new ConcurrentHashMap<Long, ConcurrentMap<RawMessage, ITSBResponseMessage>>();
    private ConcurrentMap<String, ConcurrentMap<ITSBEngineCallback, ITSBResponseMessage>> mBinds = new ConcurrentHashMap<String, ConcurrentMap<ITSBEngineCallback, ITSBResponseMessage>>();

    public TSBListenerSink() {
        // emtpy
    }

    public void register(RawMessage message, ITSBResponseMessage callBack) {
        ConcurrentMap<RawMessage, ITSBResponseMessage> map = new ConcurrentHashMap<RawMessage, ITSBResponseMessage>();
        map.put(message, callBack);
        mListeners.put(message.getRequestId(), map);
    }

    public void unregister(RawMessage message) {
        mListeners.remove(message.getRequestId());
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
            // 当网络联络连接失败后，需要将callback listener全部回调
            if (EngineConstants.EVENT_CONNECTION_CHANGE_STATUS.equals(message
                    .getBindName())
                    && message.getCode() != EngineConstants.ENGINE_CODE_SUCCESS) {
                if (!mListeners.isEmpty()) {
                    Set<Long> requestIds = mListeners.keySet();
                    for (Long requestId : requestIds) {
                        ConcurrentMap<RawMessage, ITSBResponseMessage> map = mListeners
                                .remove(requestId);
                        callbackListener(map, message);
                    }
                }
            } else {
                ConcurrentMap<RawMessage, ITSBResponseMessage> map = mListeners
                        .remove(message.getRequestId());
                // 正常处理回调事件
                callbackListener(map, message);
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
        }
    }
    
    private void callbackListener(ConcurrentMap<RawMessage, ITSBResponseMessage> map, RawMessage message) {
        if (map != null) {
            RawMessage requestMessage = map.keySet()
                    .iterator().next();
            ITSBResponseMessage callbackMessage = map.get(requestMessage);
            if (callbackMessage != null) {
                copyFromRawMessage(callbackMessage, message);
                if (EngineConstants.ENGINE_ENGINE_RESPONSE.equals(message.getName())) {
                    callbackMessage.setBindName(requestMessage.getName());
                }
                callbackMessage.callBack();
            }
            // 由于对于request请求返回的name均是"event_response",所以绑定事件是需要使用请求的name
            requestMessage.setCode(message.getCode());
            requestMessage.setErrorMessage(message.getErrorMessge());
            requestMessage.setChannel(message.getChannel());
            requestMessage.setData(message.getData());
            requestMessage.setBindName(requestMessage.getName());
            requestMessage.setName(message.getName());
            requestMessage.setServerRequestId(message.getServerRequestId());
            callbackBindListener(requestMessage);
        }
    }
    
    private void callbackBindListener(RawMessage message) {
        ConcurrentMap<ITSBEngineCallback, ITSBResponseMessage> map = mBinds.get(message.getBindName());
        if (map != null && !map.isEmpty()) {
            Iterator<ITSBResponseMessage> iterator = map.values().iterator();
            while (iterator.hasNext()) {
                ITSBResponseMessage callbackMessage = iterator.next();
                if (callbackMessage != null) {
                    copyFromRawMessage(callbackMessage, message);
                    callbackMessage.callBack();
                }
            }
        }
    }
    
    private ITSBResponseMessage copyFromRawMessage(ITSBResponseMessage response, RawMessage message) {
        response.setCode(message.getCode());
        response.setErrorMessage(message.getErrorMessge());
        response.setChannel(message.getChannel());
        response.setData(message.getData());
        response.setName(message.getName());
        response.setData(message.getData());
        response.setBindName(message.getBindName());
        response.setServerRequestId(message.getServerRequestId());
        return response;
    }
}
