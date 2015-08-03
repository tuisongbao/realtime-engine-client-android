package com.tuisongbao.engine.chat.group.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.group.ChatGroupManager;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;

import java.util.List;

public class ChatGroup implements Parcelable {
    private String groupId;
    private String owner;
    private boolean isPublic;
    private boolean userCanInvite;
    private int userCount;
    private int userCountLimit;
    private boolean isRemoved;
    private String lastActiveAt;

    transient private TSBEngine mEngine;
    transient private ChatGroupManager mGroupManager;

    public ChatGroup() {

    }

    public ChatGroup(TSBEngine engine) {
        mEngine = engine;
        mGroupManager = mEngine.getChatManager().getGroupManager();
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
    public void getUsers(TSBEngineCallback<List<ChatUser>> callback) {
        mGroupManager.getUsers(groupId, callback);
    }

    /**
     * 邀请加入群组
     *
     * @param userIds
     *            邀请加入的用户id
     * @param callback 可选
     */
    public void joinInvitation(List<String> userIds, TSBEngineCallback<String> callback) {
        mGroupManager.joinInvitation(groupId, userIds, callback);
    }

    /**
     * 删除群组中的用户
     *
     * @param userIds
     *            删除的用户id
     * @param callback 可选
     */
    public void removeUsers(List<String> userIds, TSBEngineCallback<String> callback) {
        mGroupManager.removeUsers(groupId, userIds, callback);
    }

    /**
     * 离开群组
     *
     * @param callback 可选
     */
    public void leave(TSBEngineCallback<String> callback) {
        mGroupManager.leave(groupId, callback);
    }

    @Override
    public String toString() {
        return String.format("ChatGroup[groupId: %s, owner: %s, isPublic: %s, userCanInvite: %s, userCount: %s, userCountLimit: %s"
                , groupId, owner, isPublic, userCanInvite, userCount, userCountLimit);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flag) {
        out.writeString(groupId);
        out.writeString(owner);
        out.writeInt(isPublic ? 1 : 0);
        out.writeInt(userCanInvite ? 1 : 0);
        out.writeInt(userCount);
        out.writeInt(userCountLimit);
        out.writeString(lastActiveAt);
    }

    private ChatGroup(Parcel in) {
        setGroupId(in.readString());
        setOwner(in.readString());
        setIsPublic(in.readInt() == 0 ? false : true);
        setUserCanInvite(in.readInt() == 0 ? false : true);
        setUserCount(in.readInt());
        setUserCountLimit(in.readInt());
        setLastActiveAt(in.readString());
    }

    public static final Parcelable.Creator<ChatGroup> CREATOR =
            new Parcelable.Creator<ChatGroup>() {
        @Override
        public ChatGroup createFromParcel(Parcel in) {
            return new ChatGroup(in);
        }

        @Override
        public ChatGroup[] newArray(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }
    };
}
