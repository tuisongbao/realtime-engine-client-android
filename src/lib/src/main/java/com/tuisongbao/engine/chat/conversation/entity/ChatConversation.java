package com.tuisongbao.engine.chat.conversation.entity;

import com.google.gson.Gson;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.conversation.ChatConversationManager;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageContent;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.log.LogUtil;

import java.util.List;

/**
 * <STRONG>会话类</STRONG>
 *
 * <UL>
 *     <LI>开启缓存时，所有的 API 调用会根据缓存数据适当从服务器获取最新的数据，减少流量</LI>
 *     <LI>支持序列化和反序列化，方便在 {@code Intent} 中使用</LI>
 * </UL>
 *
 * @see ChatManager#enableCache()
 */
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

    /**
     * 将合法的 JSON 字符串反序列化为 ChatConversation
     *
     * @return  ChatConversation 实例
     */
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

    /**
     * 将实例序列化为 {@code String}，可用于在 {@code Intent} 之间直接传递该实例
     *
     * @return  JSON 格式的 {@code String}
     */
    public String serialize() {
        return getSerializer().toJson(this);
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

    /**
     * 获取最后一条消息
     *
     * @return 最后一条消息
     */
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
     * 获取会话的历史消息
     *
     * <P>
     *     startMessageId 和 endMessageId 都可选，都为 {@code null} 时表示获取最新的 {@code limit} 条消息
     *
     * @param startMessageId    起始 messageId
     * @param endMessageId      结束的 messageId
     * @param limit             消息条数限制
     * @param callback          处理方法
     */
    public void getMessages(Long startMessageId,Long endMessageId, int limit,
            EngineCallback<List<ChatMessage>> callback) {
        mConversationManager.getMessages(type, target, startMessageId, endMessageId, limit, callback);
    }

    /***
     * 在会话中发送消息
     *
     * @param body      消息实体
     * @param callback  处理方法
     */
    /**
     * 在会话中发送消息
     *
     * @param content          消息实体
     * @param callback      结果处理方法
     * @param progressCallback 进度处理方法
     *
     * @return ChatMessage 实例。当为发送图片时，会将缩略图的信息填入，方便开发者刷新页面。
     */
    public ChatMessage sendMessage(ChatMessageContent content, EngineCallback<ChatMessage> callback, ProgressCallback progressCallback) {
        ChatMessage message = new ChatMessage();
        message.setContent(content)
                .setChatType(type)
                .setRecipient(target)
                .setFrom(mEngine.getChatManager().getChatUser().getUserId());
        content.generateThumbnail(200);
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
