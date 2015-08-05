package com.tuisongbao.engine.chat.message.content;

import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageContent;

/**
 * Created by root on 15-8-6.
 */
public class ChatMessageVideoContent extends ChatMessageContent {
    public ChatMessageVideoContent() {
        setType(ChatMessage.TYPE.VIDEO);
    }

    public double getDuration() {
        return getFile().getDuration();
    }
}
