package com.tuisongbao.engine.common.event.handler;

import com.google.gson.Gson;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;


public abstract class BaseEventHandler<T> implements IEventHandler<BaseEvent> {
    protected TSBEngine engine;
    private TSBEngineCallback mCallback;

    @Override
    public void setEngine(TSBEngine engine) {
        this.engine = engine;
    }

    public void setCallback(TSBEngineCallback callback) {
        mCallback = callback;
    }

    public TSBEngineCallback getCallback() {
        return mCallback;
    }

    protected T genCallbackData(BaseEvent request, RawEvent response) {
        return null;
    }

    protected T genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        return genCallbackData(request, response);
    }

    @Override
    public void onResponse(BaseEvent request, RawEvent response) {
        TSBEngineCallback callback = getCallback();
        if (callback == null) {
            return;
        }
        ResponseEventData responseData = new Gson().fromJson(response.getData(), ResponseEventData.class);
        if (responseData.getOk()) {
            T data;
            if (engine.getChatManager().isCacheEnabled()) {
                data = genCallbackDataWithCache(request, response);
            } else {
                data = genCallbackData(request, response);
            }
            ((TSBEngineCallback)callback).onSuccess(data);
        } else {
            ResponseError error = new Gson().fromJson(responseData.getError(), ResponseError.class);
            ((TSBEngineCallback) callback).onError(error);
        }
    }
}
