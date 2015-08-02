package com.tuisongbao.engine.common.event.handler;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.common.callback.ITSBEngineCallback;
import com.tuisongbao.engine.common.entity.Event;

public interface IEventHandler {
    void setCallback(ITSBEngineCallback callback);
    ITSBEngineCallback getCallback();
    void callback(Event request, Event response);
    void setEngine(TSBEngine engine);
}
