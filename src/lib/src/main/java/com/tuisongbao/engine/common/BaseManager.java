package com.tuisongbao.engine.common;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.entity.Event;
import com.tuisongbao.engine.common.event.ITSBRequestEvent;
import com.tuisongbao.engine.common.event.handler.IEventHandler;
import com.tuisongbao.engine.connection.entity.ConnectionEventData;
import com.tuisongbao.engine.log.LogUtil;

public class BaseManager extends EventEmitter {
    public static TSBEngine engine;

    private static final String TAG = BaseManager.class.getSimpleName();

    public BaseManager() {}

    public BaseManager(TSBEngine engine) {
        this.engine = engine;
        // TODO: Bind connection status sink
    }

    public boolean send(ITSBRequestEvent message, IEventHandler response) {
        try {
            Event event = engine.connection.send(message.getName(), message.serialize());
            if (response != null) {
                response.setEngine(engine);
                engine.sink.setHandler(event, response);
            }
        } catch (Exception e) {
            LogUtil.error(TAG, "Failed to send event " + message.getName());
            return false;
        }
        return true;
    }

    public <T> void handleErrorMessage(TSBEngineCallback<T> callback,
            int code, String message) {
        callback.onError(code, message);
    }

    protected void handleConnect(ConnectionEventData t) {
        // empty
    }

    protected void handleDisconnect(int code, String message) {
        // empty
    }
}
