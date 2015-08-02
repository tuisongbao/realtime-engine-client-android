package com.tuisongbao.engine.common.entity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Created by root on 15-7-31.
 */
public class Event {
    transient private Gson mGson = new Gson();

    private long id;
    private String name;
    private String channel;
    private JsonElement data;

    public Event(String name, String data) {
        this.name = name;
        this.data = mGson.fromJson(data, JsonElement.class);
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String serialize() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getChannel() {
        return channel;
    }

    public JsonElement getData() {
        return data;
    }
}
