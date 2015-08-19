package com.tuisongbao.engine.chat.message.entity.content;

import android.location.Location;

import com.google.gson.Gson;

public class ChatMessageLocationEntity {
    private double lat;
    private double lng;
    private String poi;

    public ChatMessageLocationEntity() {
    }

    public ChatMessageLocationEntity(Location location) {
        setLat(location.getLatitude());
        setLng(location.getLongitude());
    }

    public static ChatMessageLocationEntity deserialize(String string) {
        return new Gson().fromJson(string, ChatMessageLocationEntity.class);
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getPoi() {
        return poi;
    }

    public void setPoi(String poi) {
        this.poi = poi;
    }

    public String serialize() {
        return new Gson().toJson(this);
    }

    @Override
    public String toString() {
        return serialize();
    }
}
