package com.tuisongbao.engine.common.event;

import com.google.gson.Gson;

public abstract class BaseEvent<T>{
    protected long id;
    protected String channel;
    protected String name;
    protected T data;

    public BaseEvent() {};

    public BaseEvent(String name) {
        this.name = name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public T getData() {
        return this.data;
    }

    protected Gson getSerializer() {
        return new Gson();
    }

    public String serialize() {
        return getSerializer().toJson(this);
    }
}
