package com.tuisongbao.engine.chat.user.entity;

public class ChatUser {

    private boolean isNew;
    private String userId;
    private String nickname;
    private String uploadToken;
    /**
     * 在线状态， online 或 offline
     */
    private String presence;

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
        return this.uploadToken;
    }

    public void setPresence(String presence) {
        this.presence = presence;
    }

    public String getPresence() {
        return presence;
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
