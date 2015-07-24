package com.tuisongbao.engine.chat.entity;

public class TSBContactsUser {

    private String userId;
    /**
     * 在线状态， online 或 offline
     */
    private String presence;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPresence() {
        return presence;
    }

    public void setPresence(String presence) {
        this.presence = presence;
    }
}
