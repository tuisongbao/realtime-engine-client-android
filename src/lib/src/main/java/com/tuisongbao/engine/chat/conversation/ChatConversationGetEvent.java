package com.tuisongbao.engine.chat.conversation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.engine.chat.serializer.ChatTypeSerializer;
import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.common.event.BaseEvent;

class ChatConversationGetEvent extends BaseEvent<ChatConversation> {

    public ChatConversationGetEvent() {
        super("engine_chat:conversation:get");

        serializeFields.add("type");
        serializeFields.add("target");
        serializeFields.add("lastActiveAt");
    }

    @Override
    protected Gson getSerializer() {
        GsonBuilder gsonBuilder = getSerializerWithExclusionStrategy();
        gsonBuilder.registerTypeAdapter(ChatType.class, new ChatTypeSerializer());
        return gsonBuilder.create();
    }
}
