package com.tuisongbao.android.engine.common;

import com.google.gson.Gson;

public abstract class BaseTSBRequestMessage<T> implements ITSBMessage {

    private String name;
    private T data;
    
    public BaseTSBRequestMessage(String name) {
        this.name = name;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getData() {
        if (data != null) {
            Gson gson = new Gson();
            return gson.toJson(data);
        } else {
            return null;
        }
    }
}
