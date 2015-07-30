package com.tuisongbao.engine.common;

public interface TSBEngineBindCallback extends ITSBEngineCallback {
    void onEvent(String channelName, String eventName, String data);
}
