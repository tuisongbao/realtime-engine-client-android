package com.tuisongbao.engine.chat.message.content;

import com.tuisongbao.engine.chat.message.entity.ChatMessage;

public class ChatMessageImageContent extends ChatMessageMediaContent {
    public ChatMessageImageContent() {
        setType(ChatMessage.TYPE.IMAGE);
    }

    public int getWidth() {
        return getFile().getWidth();
    }

    public int getHeight() {
        return getFile().getHeight();
    }
}
