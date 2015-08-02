package com.tuisongbao.engine.engineio.source;

import com.tuisongbao.engine.engineio.pipeline.IEnginePipeline;

public interface IEngineDataSource {
    void setCallback(IEnginePipeline callback);
    void stop();
}
