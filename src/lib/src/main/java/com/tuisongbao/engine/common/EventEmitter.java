package com.tuisongbao.engine.common;

import com.github.nkzawa.emitter.Emitter;
import com.tuisongbao.engine.common.callback.TSBEngineBindCallback;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.event.handler.IEventHandler;
import com.tuisongbao.engine.log.LogUtil;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by root on 15-7-31.
 */
public class EventEmitter extends Emitter {
    private static final String TAG = "TSB" + EventEmitter.class.getSimpleName();

    private ConcurrentHashMap<TSBEngineBindCallback, Listener> listenerMap = new ConcurrentHashMap<>();

    public void bind(String event, Listener listener) {
        super.on(event, listener);
        LogUtil.verbose(TAG, "Event " + event + " has " + listeners(event).size() + " listeners");
    }

    public void unbind(String event, Listener listener) {
        super.off(event, listener);
        LogUtil.verbose(TAG, "Event " + event + " has " + listeners(event).size() + " listeners");
    }

    public void bindOnce(String event, Listener listener) {
        super.once(event, listener);
        LogUtil.verbose(TAG, "Event " + event + " has " + listeners(event).size() + " listeners");
    }

    public void trigger(String event, Object... args) {
        super.emit(event, args);
        LogUtil.verbose(TAG, "Send " + event + " to " + listeners(event).size() + " listeners");
    }

    public void bind(String event, final TSBEngineBindCallback callback) {
        Listener listener = new Listener() {
            @Override
            public void call(Object... args) {
                RawEvent rawEvent = (RawEvent)args[0];
                callback.onEvent(rawEvent.getName(), rawEvent);
            }
        };
        listenerMap.put(callback, listener);

        bind(event, listener);
    }

    public void unbind(String event) {
        super.off(event);
        LogUtil.verbose(TAG, "Event " + event + " has " + listeners(event).size() + " listeners");
    }

    public void unbind(String event, TSBEngineBindCallback callback) {
        if (callback == null) {
            unbind(event);
            return;
        }
        Listener listener = listenerMap.get(callback);
        if (listener != null) {
            unbind(event, listener);
        }
    }

    protected void bind(final String eventName, final IEventHandler response) {
        bind(eventName, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                RawEvent rawEvent = new RawEvent(eventName);
                response.onResponse(rawEvent, (RawEvent)args[0]);
            }
        });
    }
}
