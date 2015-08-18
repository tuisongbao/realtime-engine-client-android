package com.tuisongbao.engine.common.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ResponseEventData {
    private long to;
    private boolean ok;
    private JsonElement result;
    private JsonObject error;

    public void setTo(long to) {
        this.to = to;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public void setResult(JsonElement result) {
        this.result = result;
    }

    public long getTo() {
        return to;
    }

    public boolean getOk() {
        return ok;
    }

    public JsonElement getResult() {
        return result;
    }

    public JsonObject getError() {
        return error;
    }
}
