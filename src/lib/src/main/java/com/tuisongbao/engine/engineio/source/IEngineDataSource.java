package com.tuisongbao.engine.engineio.source;

import com.tuisongbao.engine.engineio.pipeline.IEngineCallback;

public interface IEngineDataSource {
    void setCallback(IEngineCallback callback);
    void stop();
}
