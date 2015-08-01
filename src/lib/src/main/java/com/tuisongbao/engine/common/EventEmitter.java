package com.tuisongbao.engine.common;

import com.github.nkzawa.emitter.Emitter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by root on 15-7-31.
 */
public class EventEmitter extends Emitter {
    private static final String TAG = EventEmitter.class.getSimpleName();
    // FIXME: 15-8-1 This map will increase more and more, because n(event) -> 1(callback) relationship is possible.
    // so can NOT remove listener from this map after unbind.
    private ConcurrentHashMap<TSBEngineBindCallback, Listener> listenerMap = new ConcurrentHashMap<>();

    public void bind(String event, Listener listener) {
        super.on(event, listener);
    }

    public void unbind(String event, Listener listener) {
        super.off(event, listener);
    }

    public void bindOnce(String event, Listener listener) {
        super.once(event, listener);
    }

    public void trigger(String event, Object... args) {
        super.emit(event, args);
    }

    public void bind(String event, final TSBEngineBindCallback callback) {
        Listener listener = new Listener() {
            @Override
            public void call(Object... args) {
                Event event = (Event)args[0];
                callback.onEvent(event.getName(), event);
            }
        };
        listenerMap.put(callback, listener);

        bind(event, listener);
    }

    public void unbind(String event) {
        super.off(event);
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
}
