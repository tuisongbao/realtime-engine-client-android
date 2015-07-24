package com.tuisongbao.engine.common;

public interface TSBEngineBindCallback extends ITSBEngineCallback {
    /**
     * @param bindName event name(or bind name)
     * @param name event name
     * @param data response data(json format)
     */
    public void onEvent(String channelName, String eventName, String data);
}
