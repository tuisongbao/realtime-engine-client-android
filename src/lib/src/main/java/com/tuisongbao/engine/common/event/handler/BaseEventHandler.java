package com.tuisongbao.engine.common.event.handler;

import com.google.gson.Gson;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.common.callback.ITSBEngineCallback;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.entity.Event;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.common.entity.ResponseEventData;


public abstract class BaseEventHandler<T> implements IEventHandler {
    transient protected TSBEngine mEngine;
    private ITSBEngineCallback mCallback;

    public void setEngine(TSBEngine engine) {
       mEngine = engine;
    }

    @Override
    public void setCallback(ITSBEngineCallback callback) {
        mCallback = callback;
    }

    @Override
    public ITSBEngineCallback getCallback() {
        return mCallback;
    }

    abstract public T parse(ResponseEventData response);

    /**
     * Called before callback event
     *
     * do things like persistent data.
     * @return T
     */
    protected T prepareCallbackData(Event request, ResponseEventData response) {
        return parse(response);
    }

    public void callback(Event request, Event response) {
        ITSBEngineCallback callback = getCallback();
        if (callback == null) {
            return;
        }
        ResponseEventData responseData = new Gson().fromJson(response.getData(), ResponseEventData.class);
        if (responseData.getOk()) {
            ((TSBEngineCallback)callback).onSuccess(prepareCallbackData(request, responseData));
        } else {
            ResponseError error = new Gson().fromJson(responseData.getError(), ResponseError.class);
            ((TSBEngineCallback) callback).onError(error.getCode(), error.getMessage());
        }
    }
}
