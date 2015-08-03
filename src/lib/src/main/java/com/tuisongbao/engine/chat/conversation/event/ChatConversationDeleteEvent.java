package com.tuisongbao.engine.chat.conversation.event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
import com.tuisongbao.engine.chat.serializer.ChatMessageChatTypeSerializer;
import com.tuisongbao.engine.common.event.BaseEvent;

public class ChatConversationDeleteEvent extends BaseEvent<ChatConversation> {

    public static final String NAME = "engine_chat:conversation:delete";

    public ChatConversationDeleteEvent() {
        super(NAME);
    }

    @Override
    protected Gson getSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class, new ChatMessageChatTypeSerializer());
        return gsonBuilder.create();
    }
}
