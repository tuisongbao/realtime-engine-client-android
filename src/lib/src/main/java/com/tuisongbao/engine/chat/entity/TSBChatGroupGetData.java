package com.tuisongbao.engine.chat.entity;

public class TSBChatGroupGetData {

    private String groupId;
    private String lastActiveAt;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(String lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }
}
