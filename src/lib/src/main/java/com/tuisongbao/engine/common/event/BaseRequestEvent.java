package com.tuisongbao.engine.common.event;

import com.google.gson.Gson;

public abstract class BaseRequestEvent<T> implements ITSBRequestEvent {

    private String name;
    private T data;

    public BaseRequestEvent(String name) {
        this.name = name;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public T getData() {
        return this.data;
    }

    /**
     * Gets serializer, it is used to serialize some special object ex, enum
     *
     * @return
     */
    protected Gson getSerializer() {
        return new Gson();
    }

    @Override
    public String serialize() {
        if (data != null) {
            Gson gson = getSerializer();
            return gson.toJson(data);
        } else {
            return null;
        }
    }
}
