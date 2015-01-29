package com.tuisongbao.android.engine.chat.entity;

import java.util.List;

public class TSBChatGroupRemoveUserData {

    private String groupId;
    private List<String> userIds;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}
