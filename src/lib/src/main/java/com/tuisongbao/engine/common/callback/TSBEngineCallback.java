package com.tuisongbao.engine.common.callback;

public interface TSBEngineCallback<T> extends ITSBEngineCallback {
    void onSuccess(T t);
    void onError(int code, String message);
}
