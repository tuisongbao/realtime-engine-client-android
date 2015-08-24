package com.tuisongbao.engine.chat.user.entity;

import com.tuisongbao.engine.common.entity.Entity;

public class ChatUser extends Entity {
    private boolean isNew;
    private String userId;
    private String nickname;
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

    public String getUploadToken() {
        return uploadToken;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return String.format("ChatUser[isNew: %s, userId: %s, uptoken: %s]"
                , isNew, userId, uploadToken);
    }
}
