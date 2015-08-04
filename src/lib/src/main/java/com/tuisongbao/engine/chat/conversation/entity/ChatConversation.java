package com.tuisongbao.engine.chat.conversation.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.ChatOptions;
import com.tuisongbao.engine.chat.conversation.ChatConversationManager;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageBody;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;

import java.util.List;

public class ChatConversation implements Parcelable {
    private ChatType type;
    private String target;
    private int unreadMessageCount;
    private String lastActiveAt;
    private ChatMessage lastMessage;

    transient private TSBEngine mEngine;
    transient private ChatConversationManager mConversationManager;

    public ChatConversation(TSBEngine engine) {
        mEngine = engine;
        mConversationManager = engine.getChatManager().getConversationManager();
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

    public ChatMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(ChatMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    /***
     * After parceling, the conversation will lost the conversation manager context. so you have to recover it after you get conversation from parcel.
     *
     * @param engine
     */
    public void setOwner(TSBEngine engine) {
        mEngine = engine;
        mConversationManager = engine.getChatManager().getConversationManager();
    }

    /***
     * 重置未读消息
     *
     * @param callback 可选
     */
    public void resetUnread(TSBEngineCallback<String> callback) {
        mConversationManager.resetUnread(type, target, callback);
    }

    /**
     * 删除会话
     *
     * @param callback 可选
     */
    public void delete(TSBEngineCallback<String> callback) {
        mConversationManager.delete(type, target, callback);
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
            TSBEngineCallback<List<ChatMessage>> callback) {
        mConversationManager.getMessages(type, target, startMessageId, endMessageId, limit, callback);
    }

    /***
     * 在会话中发送消息
     *
     * @param body
     *          消息内容
     * @param callback
     *          可选
     */
    public void sendMessage(ChatMessageBody body, TSBEngineCallback<ChatMessage> callback) {
        sendMessage(body, callback, null);
    }

    private void sendMessage(ChatMessageBody body, TSBEngineCallback<ChatMessage> callback, ChatOptions options) {
        ChatMessage message = new ChatMessage(mEngine);
        message.setBody(body).setChatType(type).setRecipient(target);
        mEngine.getChatManager().getMessageManager().sendMessage(message, callback, options);
    }

    @Override
    public String toString() {
        return String.format("ChatConversation[type: %s, target: %s, unreadMessage: %d, lastActiveAt: %s]"
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

    private ChatConversation(Parcel in) {
        setType(ChatType.getType(in.readString()));
        setTarget(in.readString());
        setUnreadMessageCount(in.readInt());
        setLastActiveAt(in.readString());
    }

    public static final Parcelable.Creator<ChatConversation> CREATOR =
            new Parcelable.Creator<ChatConversation>() {
        @Override
        public ChatConversation createFromParcel(Parcel in) {
            return new ChatConversation(in);
        }

        @Override
        public ChatConversation[] newArray(int arg0) {
            return null;
        }
    };
}
