package com.tuisongbao.engine.common;

import com.google.gson.JsonObject;

/**
 * Created by root on 15-7-31.
 */
public class ResponseEventData {
    private long to;
    private boolean ok;
    private JsonObject result;
    private JsonObject error;

    public long getTo() {
        return to;
    }

    public boolean getOk() {
        return ok;
    }

    public JsonObject getResult() {
        return result;
    }

    public JsonObject getError() {
        return error;
    }
}
