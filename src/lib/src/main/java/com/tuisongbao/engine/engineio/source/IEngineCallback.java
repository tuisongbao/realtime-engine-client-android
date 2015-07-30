package com.tuisongbao.engine.engineio.source;

import com.tuisongbao.engine.service.RawMessage;

import org.json.JSONObject;

public interface IEngineCallback {
    public void receive(JSONObject message);
}
