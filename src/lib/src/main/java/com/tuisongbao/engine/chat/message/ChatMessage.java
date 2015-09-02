package com.tuisongbao.engine.chat.message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.conversation.ChatConversation;
import com.tuisongbao.engine.chat.serializer.ChatMessageChatTypeSerializer;
import com.tuisongbao.engine.chat.serializer.ChatMessageEventTypeSerializer;
import com.tuisongbao.engine.chat.serializer.ChatMessageTypeSerializer;
import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.utils.StrUtils;

import java.util.Date;

/**
 * <STRONG>Chat 消息</STRONG>
 *
 * <UL>
 *     <LI>支持序列化和反序列化，方便在 {@code Intent} 中使用</LI>
 *     <LI>可以在 {@link com.tuisongbao.engine.chat.media.ChatVoicePlayer} 中直接播放语音类型的消息</LI>
 * </UL>
 */
public class ChatMessage {
    transient private final String TAG = "TSB" + ChatMessage.class.getSimpleName();
    /***
     * This value is not unique, it is the message's serial number in a conversation,
     * A different conversation may has a message which has a same messageId.
     */
    private long messageId;
    private ChatType type = ChatType.SingleChat;
    private String from;
    private String to;
    private ChatMessageContent content;
    private String createdAt;

    transient private Engine mEngine;

    public ChatMessage(Engine engine) {
        mEngine = engine;
    }

    public static Gson getSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class,
                new ChatMessageChatTypeSerializer());
        gsonBuilder.registerTypeAdapter(ChatMessage.TYPE.class,
                new ChatMessageTypeSerializer());
        gsonBuilder.registerTypeAdapter(ChatMessageContentSerializer.class,
                new ChatMessageContentSerializer());
        gsonBuilder.registerTypeAdapter(ChatMessageEventContent.TYPE.class, new ChatMessageEventTypeSerializer());

        return gsonBuilder.create();
    }

    /**
     * 将字符串反序列化为 ChatMessage
     *
     * @param engine        Engine 实例，用来确定 ChatMessage 的上下文
     * @param jsonString    合法的 JSON 格式 {@code String}
     * @return              ChatMessage 实例
     */
    public static ChatMessage deserialize(Engine engine, String jsonString) {
        ChatMessage message = getSerializer().fromJson(jsonString, ChatMessage.class);
        message.setEngine(engine);

        // Generate the concrete content, for accessing specific properties.
        // Gson not support this case.
        ChatMessageContent content = message.getContent();
        ChatMessageContent concreteContent = ChatMessageContent.getConcreteContent(content.getType());
        concreteContent.setEngine(engine);
        concreteContent.setText(content.getText());
        concreteContent.setFile(content.getFile());
        concreteContent.setExtra(content.getExtra());
        concreteContent.setLocation(content.getLocation());
        concreteContent.setEvent(content.getEvent());
        message.setContent(concreteContent);

        return message;
    }

    /**
     * 将实例序列化为 JSON 格式的 {@code String}，可用于在 {@code Intent} 之间直接传递该实例
     *
     * @return  JSON 格式的 {@code String}
     */
    public String serialize() {
        return getSerializer().toJson(this);
    }

    public void setEngine(Engine engine) {
        this.mEngine = engine;

        // setEngine and setContent may called in different order
        // FIXME: 15-8-20 This engine reference is a trouble, how to avoid it ?
        if (content != null) {
            content.setEngine(engine);
        }
    }

    /**
     * 获取聊天类型
     *
     * @return 单聊或群聊
     */
    public ChatType getChatType() {
        return type;
    }

    /**
     * 设置聊天类型
     *
     * @param type  聊天类型
     * @return      ChatMessage 实例
     */
    public ChatMessage setChatType(ChatType type) {
        this.type = type;
        return this;
    }

    /**
     * 获取自增消息 ID
     *
     * <P>
     *     每个 {@link ChatConversation} 都是从 0 开始。
     *
     * @return 自增消息 ID
     */
    public long getMessageId() {
        return messageId;
    }

    public ChatMessage setMessageId(long messageId) {
        this.messageId = messageId;
        return this;
    }

    /**
     * 获取消息发送者唯一标识
     *
     * @return 消息发送者唯一标识
     */
    public String getFrom() {
        return from;
    }

    /**
     * 设置消息发送者唯一标识
     *
     * @param from  消息发送者唯一标识
     * @return      已设置发送者的消息
     */
    public ChatMessage setFrom(String from) {
        this.from = from;
        return this;
    }

    /**
     * 获取消息接收者唯一标识
     *
     * @return 消息发接收者唯一标识
     */
    public String getRecipient() {
        return to;
    }

    /**
     * 设置消息接收者唯一标识
     *
     * @param to    消息接收者唯一标识
     * @return      已设置接收者的消息
     */
    public ChatMessage setRecipient(String to) {
        this.to = to;
        return this;
    }

    /**
     * 获取消息内容
     *
     * <P>
     *     ChatMessageContent 是父类，应根据 {@link ChatMessageContent#getType()} 来获取相应的内容。
     * </P>
     *
     * @return 消息内容
     */
    public ChatMessageContent getContent() {
        if (content == null) {
            return null;
        }
        content.setEngine(mEngine);
        return content;
    }

    /**
     * 设置消息内容
     *
     * @param content   消息内容
     * @return          ChatMessage 实例
     */
    public ChatMessage setContent(ChatMessageContent content) {
        this.content = content;
        this.content.setEngine(mEngine);
        return this;
    }

    /**
     * 获取消息创建时间，以服务器时间为准
     *
     * @return 创建时间，ISO8061 格式的字符串
     */
    public String getCreatedAt() {
        return createdAt;
    }

    public ChatMessage setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * 获取消息创建时间，以服务器时间为准
     *
     * @return 创建时间
     * @since v2.1.1
     */
    public Date getCreatedAtInDate() {
        if (createdAt == null) {
            return null;
        }
        return StrUtils.getDateFromTimeStringIOS8061(createdAt);
    }

    /**
     * 消息内容类型的枚举类
     */
    public enum TYPE {
        TEXT("text"),
        IMAGE("image"),
        VOICE("voice"),
        VIDEO("video"),
        EVENT("event"),
        LOCATION("location"),
        UNKNOWN("unknown");

        private final String name;

        TYPE(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static TYPE getType(String name) {
            if (!StrUtils.isEmpty(name)) {
                for (TYPE type : values()) {
                    if (type.getName().equals(name)) {
                        return type;
                    }
                }
            }
            return UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return String.format("ChatMessage[messageId: %s, from: %s, to: %s, chatType: %s, content: %s, createdAt: %s]"
                , messageId, from, to, type.getName(), content.toString(), createdAt);
    }
}
