package com.tuisongbao.engine.engineio.pipeline;

import com.tuisongbao.engine.common.entity.Event;

public interface IEngineCallback {
    void ferry(Event event);
}
