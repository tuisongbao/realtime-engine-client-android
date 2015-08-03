package com.tuisongbao.engine.common.event.handler;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.common.callback.ITSBEngineCallback;
import com.tuisongbao.engine.common.entity.RawEvent;

public interface IEventHandler<T> extends ITSBEngineCallback {
    void setEngine(TSBEngine engine);
    void onResponse(T requestEvent, RawEvent responseEvent);
}
