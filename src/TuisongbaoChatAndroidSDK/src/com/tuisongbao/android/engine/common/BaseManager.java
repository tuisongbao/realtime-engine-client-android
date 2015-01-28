package com.tuisongbao.android.engine.common;

import com.tuisongbao.android.engine.TSBEngine;

public abstract class BaseManager {

    protected boolean send(ITSBRequestMessage message) {
        return send(message, null);
    }

    protected boolean send(ITSBRequestMessage message, ITSBResponseMessage response) {
        return TSBEngine.send(message.getName(), message.getData(), response);
    }

    protected void bind(String bindName, TSBEngineBindCallback callback) {
        TSBBindResponseMessage response = new TSBBindResponseMessage();
        response.setBindCallBack(callback);
        TSBEngine.bind(bindName, response);
    }

    protected void bind(String bindName, ITSBResponseMessage response) {
        TSBEngine.bind(bindName, response);
    }

    protected void unbind(String bindName) {
        TSBEngine.unbind(bindName);
    }
}
