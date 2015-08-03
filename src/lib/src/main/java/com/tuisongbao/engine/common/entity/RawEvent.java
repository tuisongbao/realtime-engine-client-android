package com.tuisongbao.engine.common.entity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.tuisongbao.engine.common.event.BaseEvent;

/**
 * Created by root on 15-7-31.
 */
public class RawEvent extends BaseEvent<JsonElement> {

    public RawEvent(String name) {
        this.name = name;
    }

    public void setData(JsonElement data) {
        this.data = data;
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

    public boolean isOk() {
        ResponseEventData data = new Gson().fromJson(this.getData(), ResponseEventData.class);
        return data.getOk();
    }
}
