package com.tuisongbao.engine.common;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.connection.entity.TSBConnectionEvent;

import org.json.JSONException;

public class BaseManager {
    public static TSBEngine engine;

    public BaseManager() {}

    public BaseManager(TSBEngine engine) {
        this.engine = engine;
        // TODO: Bind connection status listener
    }

    public void send(ITSBRequestMessage message) throws JSONException {
        send(message, null);
    }

    public void send(ITSBRequestMessage message, ITSBResponseMessage response) throws JSONException {
        engine.connection.send(message.getName(), message.serialize(), response);
    }

    public void bind(String bindName, ITSBEngineCallback callback) {
        TSBResponseMessage response = new TSBResponseMessage();
        response.setCallback(callback);
        engine.connection.bind(bindName, response);
    }

    public void bind(String bindName, ITSBResponseMessage response) {
        engine.connection.bind(bindName, response);
    }

    public void unbind(String bindName, ITSBEngineCallback callback) {
        TSBResponseMessage response = new TSBResponseMessage();
        response.setCallback(callback);
        engine.connection.unbind(bindName, response);
    }

    public <T> void handleErrorMessage(TSBEngineCallback<T> callback,
            int code, String message) {
        callback.onError(code, message);
    }

    protected void handleConnect(TSBConnectionEvent t) {
        // empty
    }

    protected void handleDisconnect(int code, String message) {
        // empty
    }
}
