package com.tuisongbao.engine.engineio.pipeline;

import com.tuisongbao.engine.common.entity.Event;

public interface IEnginePipeline {
    void ferry(Event event);
}
