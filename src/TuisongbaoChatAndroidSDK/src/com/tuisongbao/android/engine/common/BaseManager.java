package com.tuisongbao.android.engine.common;

import com.tuisongbao.android.engine.TSBEngine;

public abstract class BaseManager {

    protected boolean send(ITSBRequestMessage message) {
        return send(message, null);
    }

    protected boolean send(ITSBRequestMessage message, ITSBResponseMessage response) {
        return TSBEngine.send(message.getName(), message.getData(), response);
    }

    public void bind(String bindName, TSBEngineBindCallback callback) {
        TSBEngine.bind(bindName, callback);
    }

    public void unbind(String bindName, TSBEngineBindCallback callback) {
        TSBEngine.unbind(bindName, callback);
    }
}
