package com.tuisongbao.engine.chat.message.content;

import com.tuisongbao.engine.chat.message.entity.ChatMessage;

public class ChatMessageVoiceContent extends ChatMessageMediaContent {
    public ChatMessageVoiceContent() {
        setType(ChatMessage.TYPE.VOICE);
    }

    public double getDuration() {
        return getFile().getDuration();
    }
}
