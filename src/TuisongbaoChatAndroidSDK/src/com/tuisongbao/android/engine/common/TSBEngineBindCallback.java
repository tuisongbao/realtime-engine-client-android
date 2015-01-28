package com.tuisongbao.android.engine.common;

public interface TSBEngineBindCallback extends ITSBEngineCallback {
    public void onEvent(String eventName, String name, String data);
}
