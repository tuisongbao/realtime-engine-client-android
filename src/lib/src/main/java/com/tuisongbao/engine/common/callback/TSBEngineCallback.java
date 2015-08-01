package com.tuisongbao.engine.common.callback;

import com.tuisongbao.engine.common.callback.ITSBEngineCallback;

public interface TSBEngineCallback<T> extends ITSBEngineCallback {
    void onSuccess(T t);
    void onError(int code, String message);
}
