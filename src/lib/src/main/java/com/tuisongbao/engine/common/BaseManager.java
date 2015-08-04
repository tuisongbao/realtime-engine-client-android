package com.tuisongbao.engine.common;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;
import com.tuisongbao.engine.connection.Connection;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.StrUtil;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BaseManager extends EventEmitter {
    public static TSBEngine engine;

    protected ConcurrentMap<BaseEvent, BaseEventHandler> pendingEvents = new ConcurrentHashMap<>();
    protected Thread retryEventsThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Iterator<BaseEvent> events = pendingEvents.keySet().iterator();
            while (events.hasNext()) {
                BaseEvent event = events.next();
                BaseEventHandler handler = pendingEvents.get(event);
                send(event, handler);
            }
        }
    });
    protected int backoffGap = 1;
    protected final int backoffGapMax = 10 * 1000;

    private static final String TAG = "TSB" + BaseManager.class.getSimpleName();

    public BaseManager() {}

    public BaseManager(TSBEngine engine) {
        this.engine = engine;
        engine.getConnection().bind(Connection.EVENT_STATE_CHANGED, new Listener() {
            @Override
            public void call(Object... args) {
                String toState = args[1].toString();
                if (StrUtil.isEqual(toState, Connection.State.Connected.getName())) {
                    connected();
                } else if (StrUtil.isEqual(toState, Connection.State.Disconnected.getName())) {
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
            LogUtil.error(TAG, "Failed to send event " + event.getName());

            if (pendingEvents.get(event) == null) {
                backoffGap = 0;
                pendingEvents.put(event, handler);
            }
            try {
                retryEventsThread.sleep(backoffGap);
                retryEventsThread.start();
                backoffGap = Math.min(backoffGapMax, backoffGap * 2);
            } catch (Exception e2) {
                LogUtil.error(TAG, e2);
            }
            return false;
        }
        return true;
    }

    protected void connected() {
        LogUtil.info(TAG, "Connected");
    }

    protected void disconnected() {
        LogUtil.info(TAG, "Disconnected");
        failedAllPendingEvents();
    }

    protected void failedAllPendingEvents() {
        try {
            retryEventsThread.interrupt();

            Iterator<BaseEvent> events = pendingEvents.keySet().iterator();
            while (events.hasNext()) {
                BaseEvent event = events.next();
                BaseEventHandler handler = pendingEvents.get(event);
                handler.getCallback().onError(null);
            }

            pendingEvents = new ConcurrentHashMap<>();
            backoffGap = 1;
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
    }
}
