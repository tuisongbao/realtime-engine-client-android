package com.tuisongbao.android.engine.common;

public interface ITSBCallback<T> {
    public void onSuccess(T t);
    public void onError(int code, String message);
}
