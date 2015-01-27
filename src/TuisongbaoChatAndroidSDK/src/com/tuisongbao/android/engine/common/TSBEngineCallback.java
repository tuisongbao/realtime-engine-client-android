package com.tuisongbao.android.engine.common;

public interface TSBEngineCallback<T> {
    public void onSuccess(T t);
    public void onError(int code, String message);
}
