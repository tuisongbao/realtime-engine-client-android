package com.tuisongbao.engine.chat.message.event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.serializer.ChatMessageChatTypeSerializer;
import com.tuisongbao.engine.chat.serializer.ChatMessageTypeSerializer;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.common.event.BaseEvent;

public class ChatMessageSendEvent extends BaseEvent<ChatMessage> {

    public static final String NAME = "engine_chat:message:send";

    public ChatMessageSendEvent() {
        super(NAME);
    }

    @Override
    protected Gson getSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class,
                new ChatMessageChatTypeSerializer());
        gsonBuilder.registerTypeAdapter(ChatMessage.TYPE.class,
                new ChatMessageTypeSerializer());
        return gsonBuilder.create();
    }

}
