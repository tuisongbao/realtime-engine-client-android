package com.tuisongbao.engine.chat.message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.engine.chat.serializer.ChatMessageChatTypeSerializer;
import com.tuisongbao.engine.chat.serializer.ChatMessageTypeSerializer;
import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.common.event.BaseEvent;

class ChatMessageGetEvent extends BaseEvent<ChatMessageGetData> {

    public ChatMessageGetEvent() {
        super("engine_chat:message:get");
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
