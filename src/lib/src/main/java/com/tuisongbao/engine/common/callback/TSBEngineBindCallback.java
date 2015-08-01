package com.tuisongbao.engine.common.callback;

import com.tuisongbao.engine.common.callback.ITSBEngineCallback;

public interface TSBEngineBindCallback extends ITSBEngineCallback {
    void onEvent(String name, Object... args);
}
