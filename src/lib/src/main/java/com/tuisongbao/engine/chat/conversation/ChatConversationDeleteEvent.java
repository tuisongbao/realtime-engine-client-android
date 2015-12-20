package com.tuisongbao.engine.chat.conversation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.engine.chat.serializer.ChatTypeSerializer;
import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.common.event.BaseEvent;

class ChatConversationDeleteEvent extends BaseEvent<ChatConversation> {

    public ChatConversationDeleteEvent() {
        super("engine_chat:conversation:delete");

        serializeFields.add("type");
        serializeFields.add("target");
    }

    @Override
    protected Gson getSerializer() {
        GsonBuilder gsonBuilder = getSerializerWithExclusionStrategy();
        gsonBuilder.registerTypeAdapter(ChatType.class, new ChatTypeSerializer());
        return gsonBuilder.create();
    }
}
