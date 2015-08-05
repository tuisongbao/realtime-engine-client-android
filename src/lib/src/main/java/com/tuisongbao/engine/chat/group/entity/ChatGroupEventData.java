package com.tuisongbao.engine.chat.group.entity;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by root on 15-8-3.
 */
public class ChatGroupEventData extends ChatGroup {
    @Expose (deserialize = false)
    private List<String> inviteUserIds;
    @Expose (deserialize = false)
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
