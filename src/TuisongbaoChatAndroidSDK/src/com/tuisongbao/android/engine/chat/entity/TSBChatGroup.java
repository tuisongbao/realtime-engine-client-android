package com.tuisongbao.android.engine.chat.entity;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.tuisongbao.android.engine.chat.TSBGroupManager;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBChatGroup implements Parcelable {

    private String groupId;
    private String owner;
    private boolean isPublic;
    private boolean userCanInvite;
    private int userCount;
    private int userCountLimit;
    private String lastActiveAt;
    private List<String> invitedUserIds;

    public TSBChatGroup() {

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
    public void getUsers(TSBEngineCallback<List<TSBContactsUser>> callback) {
        TSBGroupManager.getInstance().getUsers(groupId, callback);
    }

    /**
     * 邀请加入群组
     *
     * @param userIds
     *            邀请加入的用户id
     * @param callback 可选
     */
    public void joinInvitation(List<String> userIds, TSBEngineCallback<String> callback) {
        TSBGroupManager.getInstance().joinInvitation(groupId, userIds, callback);
    }

    /**
     * 删除群组中的用户
     *
     * @param userIds
     *            删除的用户id
     * @param callback 可选
     */
    public void removeUsers(List<String> userIds, TSBEngineCallback<String> callback) {
        TSBGroupManager.getInstance().removeUsers(groupId, userIds, callback);
    }

    /**
     * 离开群组
     *
     * @param callback 可选
     */
    public void leave(TSBEngineCallback<String> callback) {
        TSBGroupManager.getInstance().leave(groupId, callback);
    }

    @Override
    public String toString() {
        return String.format("TSBChatGroup[groupId: %s, owner: %s, isPublic: %s, userCanInvite: %s, userCount: %s, userCountLimit: %s" +
                ", invitedUserIds: %s]"
                , groupId, owner, isPublic, userCanInvite, userCount, userCountLimit, StrUtil.getStringFromList(invitedUserIds));
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
        out.writeList(invitedUserIds);
    }

    private TSBChatGroup(Parcel in) {
        setGroupId(in.readString());
        setOwner(in.readString());
        setIsPublic(in.readInt() == 0 ? false : true);
        setUserCanInvite(in.readInt() == 0 ? false : true);
        setUserCount(in.readInt());
        setUserCountLimit(in.readInt());
        setLastActiveAt(in.readString());
        in.readList(invitedUserIds, null);
    }

    public static final Parcelable.Creator<TSBChatGroup> CREATOR =
            new Parcelable.Creator<TSBChatGroup>() {
        @Override
        public TSBChatGroup createFromParcel(Parcel in) {
            return new TSBChatGroup(in);
        }

        @Override
        public TSBChatGroup[] newArray(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }
    };
}
