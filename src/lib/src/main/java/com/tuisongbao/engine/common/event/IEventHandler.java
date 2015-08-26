package com.tuisongbao.engine.common.event;

import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.common.callback.IEngineCallback;
import com.tuisongbao.engine.common.entity.RawEvent;

public interface IEventHandler<T> extends IEngineCallback {
    void setEngine(Engine engine);
    void onResponse(T requestEvent, RawEvent responseEvent);
}
