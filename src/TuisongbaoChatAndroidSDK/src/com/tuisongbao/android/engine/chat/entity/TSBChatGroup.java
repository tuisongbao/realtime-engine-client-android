package com.tuisongbao.android.engine.chat.entity;

import java.util.List;

public class TSBChatGroup {

    private String groupId;
    private String owner;
    private String name;
    private String description;
    private boolean isPublic;
    private boolean userCanInvite;
    private int userCount;
    private int userCountLimit;
    private List<String> invitedUserIds;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean userCanInvite() {
        return userCanInvite;
    }

    public void setUserCanInvite(boolean userCanInvite) {
        this.userCanInvite = userCanInvite;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public int getUserCountLimit() {
        return userCountLimit;
    }

    public void setUserCountLimit(int userCountLimit) {
        this.userCountLimit = userCountLimit;
    }

    public List<String> getInvitedUserIds() {
        return invitedUserIds;
    }

    public void setInvitedUserIds(List<String> invitedUserIds) {
        this.invitedUserIds = invitedUserIds;
    }

    @Override
    public String toString() {
        return String.format("TSBChatGroup[groupId: %s, owner: %s, name: %s, description: %s, isPublic: %s, userCanInvite: %s, userCount: %s, userCountLimit: %s]"
                , groupId, owner, name, description, isPublic, userCanInvite, userCount, userCountLimit);
    }
}
