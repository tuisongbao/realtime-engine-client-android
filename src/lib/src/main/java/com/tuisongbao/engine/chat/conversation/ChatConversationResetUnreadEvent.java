package com.tuisongbao.engine.chat.conversation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.engine.chat.serializer.ChatMessageChatTypeSerializer;
import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.common.event.BaseEvent;

class ChatConversationResetUnreadEvent extends BaseEvent<ChatConversation> {

    public ChatConversationResetUnreadEvent() {
        super("engine_chat:conversation:resetUnread");

        serializeFields.add("type");
        serializeFields.add("target");
    }

    @Override
    protected Gson getSerializer() {
        GsonBuilder gsonBuilder = getSerializerWithExclusionStrategy();
        gsonBuilder.registerTypeAdapter(ChatType.class, new ChatMessageChatTypeSerializer());
        return gsonBuilder.create();
    }

}
