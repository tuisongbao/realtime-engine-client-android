package com.tuisongbao.engine.chat.entity;

import java.util.HashMap;

import com.tuisongbao.engine.chat.entity.TSBMessage.TYPE;

public class TSBChatUser {

    private boolean isNew;
    private String userId;
    private String uploadToken;

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

    public void setUploadToken(String uploadToken) {
        this.uploadToken = uploadToken;
    }

    public String getUploadToken() {
        return this.uploadToken;
    }

    @Override
    public String toString() {
        return String.format("TSBChatUser[isNew: %s, userId: %s, uptoken: %s]"
                , isNew, userId, uploadToken);
    }
}
