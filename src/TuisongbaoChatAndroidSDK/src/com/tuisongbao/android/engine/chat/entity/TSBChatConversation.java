package com.tuisongbao.android.engine.chat.entity;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.TSBConversationManager;
import com.tuisongbao.android.engine.common.TSBEngineCallback;

public class TSBChatConversation implements Parcelable {
    private ChatType type;
    private String target;
    private int unreadMessageCount;
    private String lastActiveAt;

    public TSBChatConversation() {

    }

    public ChatType getType() {
        return type;
    }

    public void setType(ChatType type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public String getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(String lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    /***
     * 重置未读消息
     *
     * @param callback 可选
     */
    public void resetUnread(TSBEngineCallback<String> callback) {
        TSBConversationManager.getInstance().resetUnread(type, target, callback);
    }

    /**
     * 删除会话
     *
     * @param callback 可选
     */
    public void delete(TSBEngineCallback<String> callback) {
        TSBConversationManager.getInstance().delete(type, target, callback);
    }

    /**
     * 获取消息
     *
     * @param startMessageId
     *            可选
     * @param endMessageId
     *            可选
     * @param limit
     *            可选，默认 20，最大 100
     * @param callback 可选
     */
    public void getMessages(Long startMessageId,Long endMessageId, int limit,
            TSBEngineCallback<List<TSBMessage>> callback) {
        TSBConversationManager.getInstance().getMessages(type, target, startMessageId, endMessageId, limit, callback);
    }

    /***
     * 在会话中发送消息
     *
     * @param body
     *          消息内容
     * @param callback
     *          可选
     */
    public void sendMessage(TSBMessageBody body, TSBEngineCallback<TSBMessage> callback) {
        sendMessage(body, callback, null);
    }

    private void sendMessage(TSBMessageBody body, TSBEngineCallback<TSBMessage> callback, TSBChatOptions options) {
        TSBMessage message = new TSBMessage();
        message.setBody(body).setChatType(type).setRecipient(target);
        TSBChatManager.getInstance().sendMessage(message, callback, options);
    }

    @Override
    public String toString() {
        return String.format("TSBChatConversation[type: %s, target: %s, unreadMessage: %d, lastActiveAt: %s]"
                , type.getName(), target, unreadMessageCount, lastActiveAt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flag) {
        out.writeString(type.getName());
        out.writeString(target);
        out.writeInt(unreadMessageCount);
        out.writeString(lastActiveAt);
    }

    private TSBChatConversation(Parcel in) {
        setType(ChatType.getType(in.readString()));
        setTarget(in.readString());
        setUnreadMessageCount(in.readInt());
        setLastActiveAt(in.readString());
    }

    public static final Parcelable.Creator<TSBChatConversation> CREATOR =
            new Parcelable.Creator<TSBChatConversation>() {
        @Override
        public TSBChatConversation createFromParcel(Parcel in) {
            return new TSBChatConversation(in);
        }

        @Override
        public TSBChatConversation[] newArray(int arg0) {
            return null;
        }
    };
}
