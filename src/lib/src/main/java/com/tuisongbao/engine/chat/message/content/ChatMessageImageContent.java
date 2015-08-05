package com.tuisongbao.engine.chat.message.content;

import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageContent;

/**
 * Created by root on 15-8-6.
 */
public class ChatMessageImageContent extends ChatMessageContent {
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
