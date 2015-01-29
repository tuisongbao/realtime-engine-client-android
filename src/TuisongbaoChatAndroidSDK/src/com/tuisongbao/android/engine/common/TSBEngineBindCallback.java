package com.tuisongbao.android.engine.common;

public interface TSBEngineBindCallback extends ITSBEngineCallback {
    /**
     * @param bindName event name(or bind name)
     * @param name event name
     * @param data response data(json format)
     */
    public void onEvent(String bindName, String name, String data);
}
