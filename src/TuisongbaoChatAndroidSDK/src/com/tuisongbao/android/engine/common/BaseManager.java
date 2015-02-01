package com.tuisongbao.android.engine.common;

import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.connection.entity.TSBConnection;

public abstract class BaseManager {

    protected boolean send(ITSBRequestMessage message) {
        return send(message, null);
    }

    protected boolean send(ITSBRequestMessage message, ITSBResponseMessage response) {
        return TSBEngine.send(message.getName(), message.serialize(), response);
    }

    protected boolean send(String name, String data, ITSBResponseMessage response) {
        return TSBEngine.send(name, data, null);
    }

    protected void bind(String bindName, ITSBEngineCallback callback) {
        TSBResponseMessage response = new TSBResponseMessage();
        response.setCallback(callback);
        TSBEngine.bind(bindName, response);
    }

    protected void bind(String bindName, ITSBResponseMessage response) {
        TSBEngine.bind(bindName, response);
    }

    protected void unbind(String bindName) {
        TSBEngine.unbind(bindName);
    }

    protected void unbind(String bindName, ITSBEngineCallback callback) {
        TSBResponseMessage response = new TSBResponseMessage();
        response.setCallback(callback);
        TSBEngine.unbind(bindName, response);
    }

    protected BaseManager() {
        bindConnectionEvents();
    }

    private void bindConnectionEvents() {
        TSBEngine.connection.bindConnectionChangeStatusEvent(mConnectionCallback);
    }

    private TSBEngineCallback<TSBConnection> mConnectionCallback = new TSBEngineCallback<TSBConnection>() {

        @Override
        public void onSuccess(TSBConnection t) {
            handleConnect(t);
        }

        @Override
        public void onError(int code, String message) {
            handleDisconnect(code, message);
        }
    };

    protected void handleConnect(TSBConnection t) {
        // empty
    }

    protected void handleDisconnect(int code, String message) {
        // empty
    }
}
