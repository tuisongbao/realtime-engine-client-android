package com.tuisongbao.engine.common.event.handler;

import com.google.gson.Gson;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.common.callback.ITSBEngineCallback;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;


public abstract class BaseEventHandler<T> implements IEventHandler<BaseEvent> {
    protected TSBEngine mEngine;
    private ITSBEngineCallback mCallback;

    @Override
    public void setEngine(TSBEngine engine) {
        mEngine = engine;
    }

    public void setCallback(ITSBEngineCallback callback) {
        mCallback = callback;
    }

    public ITSBEngineCallback getCallback() {
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
        ITSBEngineCallback callback = getCallback();
        if (callback == null) {
            return;
        }
        ResponseEventData responseData = new Gson().fromJson(response.getData(), ResponseEventData.class);
        if (responseData.getOk()) {
            T data;
            if (mEngine.chatManager.isCacheEnabled()) {
                data = genCallbackDataWithCache(request, response);
            } else {
                data = genCallbackData(request, response);
            }
            ((TSBEngineCallback)callback).onSuccess(data);
        } else {
            ResponseError error = new Gson().fromJson(responseData.getError(), ResponseError.class);
            ((TSBEngineCallback) callback).onError(error.getCode(), error.getMessage());
        }
    }
}