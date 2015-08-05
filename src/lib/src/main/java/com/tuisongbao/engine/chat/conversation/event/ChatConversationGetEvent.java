package com.tuisongbao.engine.chat.conversation.event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
import com.tuisongbao.engine.chat.serializer.ChatMessageChatTypeSerializer;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.common.event.BaseEvent;

public class ChatConversationGetEvent extends BaseEvent<ChatConversation> {

    public ChatConversationGetEvent() {
        super("engine_chat:conversation:get");

        serializeFields.add("type");
        serializeFields.add("target");
        serializeFields.add("lastActiveAt");
    }

    @Override
    protected Gson getSerializer() {
        GsonBuilder gsonBuilder = getSerializerWithExclusionStrategy();
        gsonBuilder.registerTypeAdapter(ChatType.class, new ChatMessageChatTypeSerializer());
        return gsonBuilder.create();
    }
}
