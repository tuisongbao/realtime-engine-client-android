package com.tuisongbao.engine.chat.message;

import com.google.gson.Gson;
import com.tuisongbao.engine.common.event.BaseEvent;

class ChatMessageSendEvent extends BaseEvent<ChatMessage> {

    public ChatMessageSendEvent() {
        super("engine_chat:message:send");
    }

    @Override
    protected Gson getSerializer() {
        return ChatMessage.getSerializer();
    }
}
