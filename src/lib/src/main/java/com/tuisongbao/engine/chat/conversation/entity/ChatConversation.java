package com.tuisongbao.engine.chat.conversation.entity;

import com.google.gson.Gson;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.conversation.ChatConversationManager;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageContent;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.log.LogUtil;

import java.util.List;

public class ChatConversation {
    transient private static final String TAG = ChatConversation.class.getSimpleName();

    private ChatType type;
    private String target;
    private int unreadMessageCount;
    private String lastActiveAt;
    private ChatMessage lastMessage;

    transient private Engine mEngine;
    transient private ChatConversationManager mConversationManager;

    public ChatConversation(Engine engine) {
        mEngine = engine;
        mConversationManager = engine.getChatManager().getConversationManager();
    }

    public static ChatConversation deserialize(Engine engine, String jsonString) {
        try {
            ChatConversation conversation = getSerializer().fromJson(jsonString, ChatConversation.class);
            conversation.mEngine = engine;
            conversation.mConversationManager = engine.getChatManager().getConversationManager();

            return conversation;
        } catch (Exception e) {
            LogUtil.error(TAG, e);
            return null;
        }
    }

    public String serialize() {
        String stream = getSerializer().toJson(this);
        return stream;
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

    public void incUnreadMessageCount() {
        unreadMessageCount++;
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
     * 重置未读消息
     *
     * @param callback 可选
     */
    public void resetUnread(EngineCallback<String> callback) {
        mConversationManager.resetUnread(type, target, callback);
    }

    /**
     * 删除会话
     *
     * @param callback 可选
     */
    public void delete(EngineCallback<String> callback) {
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
            EngineCallback<List<ChatMessage>> callback) {
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
    public ChatMessage sendMessage(ChatMessageContent body, EngineCallback<ChatMessage> callback) {
        return sendMessage(body, callback, null);
    }

    public ChatMessage sendMessage(ChatMessageContent body, EngineCallback<ChatMessage> callback, ProgressCallback progressCallback) {
        ChatMessage message = new ChatMessage();
        message.setContent(body)
                .setChatType(type)
                .setRecipient(target)
                .setFrom(mEngine.getChatManager().getChatUser().getUserId());
        message.generateThumbnail(200);
        return mEngine.getChatManager().getMessageManager().sendMessage(message, callback, progressCallback);
    }

    private static Gson getSerializer() {
        return ChatMessage.getSerializer();
    }

    @Override
    public String toString() {
        return String.format("ChatConversation[type: %s, target: %s, unreadMessage: %d, lastActiveAt: %s]"
                , type.getName(), target, unreadMessageCount, lastActiveAt);
    }
}
