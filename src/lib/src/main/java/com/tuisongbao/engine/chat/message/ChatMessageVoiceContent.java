package com.tuisongbao.engine.chat.message;

public class ChatMessageVoiceContent extends ChatMessageMediaContent {
    public ChatMessageVoiceContent() {
        setType(ChatMessage.TYPE.VOICE);
    }

    public double getDuration() {
        return getFile().getDuration();
    }
}
