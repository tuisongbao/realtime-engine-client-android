package com.tuisongbao.android.engine.chat.entity;

import java.util.List;

public class TSBChatGroupCreateData {

    private String name;
    private String description;
    /**
     * 默认值 true ，任何用户的加群请求都会直接通过，无需审核
     */
    private boolean isPublic = true;
    /**
     * 默认值 true ，除创建者（owner）外，其他群用户也可以发送加群邀请
     */
    private boolean userCanInvite = true;
    private List<String> inviteUserIds;

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

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isUserCanInvite() {
        return userCanInvite;
    }

    public void setUserCanInvite(boolean userCanInvite) {
        this.userCanInvite = userCanInvite;
    }

    public List<String> getInviteUserIds() {
        return inviteUserIds;
    }

    public void setInviteUserIds(List<String> inviteUserIds) {
        this.inviteUserIds = inviteUserIds;
    }
}
