package com.tuisongbao.engine.chat.message;

public class ChatMessageVideoContent extends ChatMessageMediaContent {
    public ChatMessageVideoContent() {
        setType(ChatMessage.TYPE.VIDEO);
    }

    public double getDuration() {
        return getFile().getDuration();
    }
}
