package com.tuisongbao.engine.chat.message.event.handler;

import com.google.gson.Gson;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.db.ChatConversationDataSource;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageContent;
import com.tuisongbao.engine.chat.message.event.ChatMessageSendEvent;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

/**
 * Created by root on 15-8-2.
 */
public class ChatMessageSendEventHandler extends BaseEventHandler<ChatMessage> {

    @Override
    protected ChatMessage genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        String userId = engine.getChatManager().getChatUser().getUserId();
        ChatMessage responseMessage = genCallbackData(request, response);
        ChatMessage sentMessage = ((ChatMessageSendEvent)request).getData();

        ChatMessageContent responseMessageContent = responseMessage.getContent();
        if (responseMessageContent != null) {
            // Media message, get download url and update DB
            ChatMessageContent content = sentMessage.getContent();
            content.getFile().setUrl(responseMessageContent.getFile().getUrl());
            content.getFile().setThumbUrl(responseMessageContent.getFile().getThumbUrl());
        }
        sentMessage.setFrom(userId);

        if (sentMessage != null && engine.getChatManager().isCacheEnabled()) {
            ChatConversationDataSource dataSource = new ChatConversationDataSource(TSBEngine.getContext(), engine);
            dataSource.open();
            dataSource.upsertMessage(userId, sentMessage);
            dataSource.close();
        }

        sentMessage.setEngine(engine);
        return sentMessage;
    }

    @Override
    public ChatMessage genCallbackData(BaseEvent request, RawEvent response) {
        ResponseEventData data = new Gson().fromJson(response.getData(), ResponseEventData.class);
        return ChatMessage.getSerializer().fromJson(data.getResult(), ChatMessage.class);
    }
}
