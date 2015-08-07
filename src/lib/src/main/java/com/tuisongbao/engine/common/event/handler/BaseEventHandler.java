package com.tuisongbao.engine.common.event.handler;

import com.google.gson.Gson;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;


public abstract class BaseEventHandler<T> implements IEventHandler<BaseEvent> {
    protected Engine engine;
    private EngineCallback mCallback;

    @Override
    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setCallback(EngineCallback callback) {
        mCallback = callback;
    }

    public EngineCallback getCallback() {
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
        EngineCallback callback = getCallback();
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
            ((EngineCallback)callback).onSuccess(data);
        } else {
            ResponseError error = new Gson().fromJson(responseData.getError(), ResponseError.class);
            ((EngineCallback) callback).onError(error);
        }
    }
}
