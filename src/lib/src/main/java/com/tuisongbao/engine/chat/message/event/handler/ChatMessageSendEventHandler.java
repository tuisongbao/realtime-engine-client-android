package com.tuisongbao.engine.chat.message.event.handler;

import com.google.gson.Gson;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.db.ChatConversationDataSource;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageContent;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageFileEntity;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageLocationEntity;
import com.tuisongbao.engine.chat.message.event.ChatMessageSendEvent;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

public class ChatMessageSendEventHandler extends BaseEventHandler<ChatMessage> {

    @Override
    protected ChatMessage genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        String userId = engine.getChatManager().getChatUser().getUserId();
        ChatMessage responseMessage = genCallbackData(request, response);
        ChatMessage sentMessage = ((ChatMessageSendEvent)request).getData();

        ChatMessageContent responseMessageContent = responseMessage.getContent();
        if (responseMessageContent != null) {
            // Get file and location entity from response message
            ChatMessageFileEntity fileInResponseMessage = responseMessageContent.getFile();
            ChatMessageLocationEntity locationInResponseMessage = responseMessageContent.getLocation();

            // Get file and location entity from sent message
            ChatMessageFileEntity fileInSentMessage = sentMessage.getContent().getFile();
            ChatMessageLocationEntity locationInSentMessage = sentMessage.getContent().getLocation();

            // Merge file and location properties into sent message
            if (fileInResponseMessage != null) {
                // Media message, get download url and update DB
                fileInSentMessage.setUrl(fileInResponseMessage.getUrl());
                fileInSentMessage.setThumbUrl(fileInResponseMessage.getThumbUrl());
            }
            if (locationInResponseMessage != null) {
                locationInSentMessage.setPoi(locationInResponseMessage.getPoi());
            }
        } else {
            // Send text message successfully
        }

        sentMessage.setFrom(userId);
        sentMessage.setMessageId(responseMessage.getMessageId());

        if (engine.getChatManager().isCacheEnabled()) {
            ChatConversationDataSource dataSource = new ChatConversationDataSource(Engine.getContext(), engine);
            dataSource.open();
            dataSource.upsertMessage(userId, sentMessage);
            dataSource.close();
        }
        return sentMessage;
    }

    @Override
    public ChatMessage genCallbackData(BaseEvent request, RawEvent response) {
        ResponseEventData data = new Gson().fromJson(response.getData(), ResponseEventData.class);
        return ChatMessage.getSerializer().fromJson(data.getResult(), ChatMessage.class);
    }
}
