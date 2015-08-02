package com.tuisongbao.engine.chat.message.event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageTypeSerializer;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.common.event.BaseRequestEvent;

public class ChatMessageSendEvent extends
        BaseRequestEvent<ChatMessage> {

    public static final String NAME = "engine_chat:message:send";

    public ChatMessageSendEvent() {
        super(NAME);
    }

    @Override
    protected Gson getSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class,
                new TSBChatMessageChatTypeSerializer());
        gsonBuilder.registerTypeAdapter(ChatMessage.TYPE.class,
                new TSBChatMessageTypeSerializer());
        return gsonBuilder.create();
    }

}
