package com.tuisongbao.engine.chat.message.content;

import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageContent;

public class ChatMessageVoiceContent extends ChatMessageContent {
    public ChatMessageVoiceContent() {
        setType(ChatMessage.TYPE.VOICE);
    }

    public double getDuration() {
        return getFile().getDuration();
    }
}
