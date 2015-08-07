package com.tuisongbao.engine.common.callback;

import com.tuisongbao.engine.common.entity.ResponseError;

public interface EngineCallback<T> {
    void onSuccess(T t);
    void onError(ResponseError error);
}
