package com.tuisongbao.engine.channel.entity;

import com.google.gson.JsonObject;

/**
 * Created by root on 15-8-13.
 */
public class OnlineUser {
    private String userId;
    private JsonObject userInfo;

    public JsonObject getUserInfo() {
        return userInfo;
    }

    public String getUserId() {
        return userId;
    }
}
