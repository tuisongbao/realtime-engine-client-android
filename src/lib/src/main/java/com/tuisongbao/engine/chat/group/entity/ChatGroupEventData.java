package com.tuisongbao.engine.chat.group.entity;

import java.util.List;

/**
 * Created by root on 15-8-3.
 */
public class ChatGroupEventData extends ChatGroup {
    private List<String> inviteUserIds;
    private List<String> userIds;

    public void setInviteUserIds(List<String> inviteUserIds) {
        this.inviteUserIds = inviteUserIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public List<String> getInviteUserIds() {
        return inviteUserIds;
    }
}
