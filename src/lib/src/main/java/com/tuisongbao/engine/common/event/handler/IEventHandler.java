package com.tuisongbao.engine.common.event.handler;

import com.tuisongbao.engine.common.callback.ITSBEngineCallback;
import com.tuisongbao.engine.common.entity.Event;
import com.tuisongbao.engine.common.entity.ResponseEventData;

public interface IEventHandler {
    void setCallback(ITSBEngineCallback callback);
    ITSBEngineCallback getCallback();
    void callback(Event request, ResponseEventData response);
}
