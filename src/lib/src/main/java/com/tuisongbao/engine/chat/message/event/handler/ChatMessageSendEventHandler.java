package com.tuisongbao.engine.chat.message.event.handler;

import com.google.gson.Gson;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.db.ChatConversationDataSource;
import com.tuisongbao.engine.chat.message.entity.ChatMediaMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
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
        String userId = mEngine.chatManager.getChatUser().getUserId();
        ChatMessage responseMessage = genCallbackData(request, response);
        ChatMessage sentMessage = ((ChatMessageSendEvent)request).getData();

        if (responseMessage.getBody() != null) {
            // Media message, get download url and update DB
            ChatMediaMessageBody body = (ChatMediaMessageBody)sentMessage.getBody();
            String downloadUrl = ((ChatMediaMessageBody)responseMessage.getBody()).getDownloadUrl();
            body.setDownloadUrl(downloadUrl);

            sentMessage.setBody(body);
        }
        sentMessage.setFrom(userId);

        if (sentMessage != null && mEngine.chatManager.isCacheEnabled()) {
            ChatConversationDataSource dataSource = new ChatConversationDataSource(TSBEngine.getContext(), mEngine);
            dataSource.open();
            dataSource.upsertMessage(userId, sentMessage);
            dataSource.close();
        }

        sentMessage.setEngine(mEngine);
        return sentMessage;
    }

    @Override
    public ChatMessage genCallbackData(BaseEvent request, RawEvent response) {
        ResponseEventData data = new Gson().fromJson(response.getData(), ResponseEventData.class);
        return ChatMessage.getSerializer().fromJson(data.getResult(), ChatMessage.class);
    }
}
