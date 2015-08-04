package com.tuisongbao.engine.common;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.IEventHandler;
import com.tuisongbao.engine.connection.Connection;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.StrUtil;

public class BaseManager extends EventEmitter {
    public static TSBEngine engine;

    private static final String TAG = "TSB" + BaseManager.class.getSimpleName();

    public BaseManager() {}

    public BaseManager(TSBEngine engine) {
        this.engine = engine;
        engine.getConnection().bind(Connection.ConnectionEvent.StateChanged, new Listener() {
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

    public boolean send(BaseEvent event, IEventHandler response) {
        try {
            BaseEvent sentEvent = engine.getConnection().send(event);
            if (response != null) {
                response.setEngine(engine);
                engine.getSink().setHandler(sentEvent, response);
            }
        } catch (Exception e) {
            LogUtil.error(TAG, "Failed to send event " + event.getName());
            return false;
        }
        return true;
    }

    protected void connected() {
        LogUtil.info(TAG, "Connected");
    }

    protected void disconnected() {
        LogUtil.info(TAG, "Disconnected");
    }
}
