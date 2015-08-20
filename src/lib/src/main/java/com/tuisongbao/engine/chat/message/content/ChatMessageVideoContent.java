package com.tuisongbao.engine.chat.message.content;

import com.tuisongbao.engine.chat.message.entity.ChatMessage;

public class ChatMessageVideoContent extends ChatMessageMediaContent {
    public ChatMessageVideoContent() {
        setType(ChatMessage.TYPE.VIDEO);
    }

    public double getDuration() {
        return getFile().getDuration();
    }
}
