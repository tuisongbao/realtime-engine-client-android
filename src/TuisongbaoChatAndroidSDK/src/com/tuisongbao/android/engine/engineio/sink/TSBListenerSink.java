package com.tuisongbao.android.engine.engineio.sink;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.tuisongbao.android.engine.common.ITSBResponseMessage;
import com.tuisongbao.android.engine.common.TSBEngineBindCallback;
import com.tuisongbao.android.engine.service.RawMessage;
import com.tuisongbao.android.engine.util.StrUtil;

/**
 * A data sink that sends new messages of specific types to listeners.
 * 
 */
public class TSBListenerSink extends BaseEngineCallbackSink {
    private final static String TAG = "propagate";

    private ConcurrentMap<String, ConcurrentMap<RawMessage, ITSBResponseMessage>> mListeners = new ConcurrentHashMap<String, ConcurrentMap<RawMessage, ITSBResponseMessage>>();
    private ConcurrentMap<String, ITSBResponseMessage> mBinds = new ConcurrentHashMap<String, ITSBResponseMessage>();

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

    public void bind(String name, ITSBResponseMessage callBack) {
        mBinds.put(name, callBack);
    }

    public void unbind(String name) {
        mBinds.remove(name);
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
            if (!StrUtil.isEmpty(message.getBindName())) {
                ITSBResponseMessage callbackMessage = mBinds.get(message.getBindName());
                if (message.isUnbind()) {
                    unbind(message.getBindName());
                }
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
