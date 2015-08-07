package com.tuisongbao.engine.chat.group.entity;

import com.google.gson.Gson;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.group.ChatGroupManager;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.common.callback.EngineCallback;

import java.util.List;

public class ChatGroup {
    private String groupId;
    private String owner;
    private boolean isPublic;
    private boolean userCanInvite;
    private int userCount;
    private int userCountLimit;
    private boolean isRemoved;
    private String lastActiveAt;

    transient private Engine mEngine;
    transient private ChatGroupManager mGroupManager;

    public ChatGroup() {

    }

    public ChatGroup(Engine engine) {
        mEngine = engine;
        mGroupManager = mEngine.getChatManager().getGroupManager();
    }

    public static ChatGroup deserialize(Engine engine, String jsonString) {
        ChatGroup group = getSerializer().fromJson(jsonString, ChatGroup.class);
        group.mEngine = engine;
        group.mGroupManager = engine.getChatManager().getGroupManager();

        return group;
    }

    public String serialize() {
        return getSerializer().toJson(this);
    }

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

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
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

    public boolean getIsRemoved() {
        return isRemoved;
    }

    public String getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(String lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    /**
     * 获取群组下用户列表，会从服务器同步最新的数据
     *
     * @param callback 可选
     */
    public void getUsers(EngineCallback<List<ChatUser>> callback) {
        mGroupManager.getUsers(groupId, callback);
    }

    /**
     * 邀请加入群组
     *
     * @param userIds
     *            邀请加入的用户id
     * @param callback 可选
     */
    public void joinInvitation(List<String> userIds, EngineCallback<String> callback) {
        mGroupManager.joinInvitation(groupId, userIds, callback);
    }

    /**
     * 删除群组中的用户
     *
     * @param userIds
     *            删除的用户id
     * @param callback 可选
     */
    public void removeUsers(List<String> userIds, EngineCallback<String> callback) {
        mGroupManager.removeUsers(groupId, userIds, callback);
    }

    /**
     * 离开群组
     *
     * @param callback 可选
     */
    public void leave(EngineCallback<String> callback) {
        mGroupManager.leave(groupId, callback);
    }

    private static Gson getSerializer() {
        return new Gson();
    }

    @Override
    public String toString() {
        return String.format("ChatGroup[groupId: %s, owner: %s, isPublic: %s, userCanInvite: %s, userCount: %s, userCountLimit: %s"
                , groupId, owner, isPublic, userCanInvite, userCount, userCountLimit);
    }
}
