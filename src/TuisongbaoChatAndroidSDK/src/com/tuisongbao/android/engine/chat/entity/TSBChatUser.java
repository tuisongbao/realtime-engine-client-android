package com.tuisongbao.android.engine.chat.entity;

public class TSBChatUser {

    private boolean isNew;
    private String userId;

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
