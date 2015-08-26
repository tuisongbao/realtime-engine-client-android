package com.tuisongbao.engine.common;

import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.BaseEventHandler;
import com.tuisongbao.engine.connection.Connection;
import com.tuisongbao.engine.utils.LogUtils;
import com.tuisongbao.engine.utils.StrUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BaseManager extends EventEmitter {
    protected static Engine engine;

    protected ConcurrentMap<BaseEvent, BaseEventHandler> pendingEvents = new ConcurrentHashMap<>();
    protected int backoffGap = 1;
    protected final int backoffGapMax = 10 * 1000;

    private static final String TAG = "TSB" + BaseManager.class.getSimpleName();

    protected BaseManager() {}

    protected BaseManager(Engine engine) {
        BaseManager.engine = engine;
        engine.getConnection().bind(Connection.EVENT_STATE_CHANGED, new Listener() {
            @Override
            public void call(Object... args) {
                String toState = args[1].toString();
                if (StrUtils.isEqual(toState, Connection.State.Connected.getName())) {
                    connected();
                } else if (StrUtils.isEqual(toState, Connection.State.Disconnected.getName())) {
                    disconnected();
                }
            }
        });
    }

    public boolean send(BaseEvent event, BaseEventHandler handler) {
        try {
            BaseEvent sentEvent = engine.getConnection().send(event);
            if (handler != null) {
                handler.setEngine(engine);
                engine.getSink().setHandler(sentEvent, handler);
            }
        } catch (Exception e1) {
            LogUtils.error(TAG, "Failed to send event " + event.getName());
            return false;
        }
        return true;
    }

    protected void connected() {
        LogUtils.info(TAG, "Connected");
    }

    protected void disconnected() {
        LogUtils.info(TAG, "Disconnected");
    }
}
