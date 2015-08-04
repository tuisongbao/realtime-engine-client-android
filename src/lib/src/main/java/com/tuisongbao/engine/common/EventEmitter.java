package com.tuisongbao.engine.common;

import com.github.nkzawa.emitter.Emitter;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.event.handler.IEventHandler;
import com.tuisongbao.engine.log.LogUtil;

/**
 * Created by root on 15-7-31.
 */
public class EventEmitter extends Emitter {
    private static final String TAG = "TSB" + EventEmitter.class.getSimpleName();

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
        LogUtil.verbose(TAG, "Send " + event + " to " + listeners(event).size() + " listeners");
        super.emit(event, args);
    }

    public void unbind(String event) {
        super.off(event);
        LogUtil.verbose(TAG, "Event " + event + " has " + listeners(event).size() + " listeners");
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
