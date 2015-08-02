package com.tuisongbao.engine.common.callback;

public interface TSBEngineBindCallback extends ITSBEngineCallback {
    void onEvent(String name, Object... args);
}
