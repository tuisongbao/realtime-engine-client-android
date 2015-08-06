package com.tuisongbao.engine.chat.message.event;

import com.google.gson.Gson;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.common.event.BaseEvent;

public class ChatMessageSendEvent extends BaseEvent<ChatMessage> {

    public ChatMessageSendEvent() {
        super("engine_chat:message:send");
    }

    @Override
    protected Gson getSerializer() {
        return ChatMessage.getSerializer();
    }
}
