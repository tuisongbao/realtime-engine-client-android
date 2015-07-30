package com.tuisongbao.engine.engineio.source;

public interface IEngineDataSource {
    void setCallback(IEngineCallback callback);
    void stop();
}
