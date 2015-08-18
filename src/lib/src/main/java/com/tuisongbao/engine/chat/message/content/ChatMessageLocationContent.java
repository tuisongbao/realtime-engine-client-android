package com.tuisongbao.engine.chat.message.content;

import android.location.Location;

import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageContent;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageLocationEntity;

public class ChatMessageLocationContent extends ChatMessageContent {
    public ChatMessageLocationContent(Location location) {
        setType(ChatMessage.TYPE.LOCATION);

        ChatMessageLocationEntity entity = new ChatMessageLocationEntity(location);
        setLocation(entity);
    }
}
