package com.tuisongbao.engine.chat.message.event.handler;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.db.TSBConversationDataSource;
import com.tuisongbao.engine.chat.message.entity.ChatMediaMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.common.entity.Event;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

/**
 * Created by root on 15-8-2.
 */
public class ChatMessageSendEventHandler extends BaseEventHandler<ChatMessage> {

    @Override
    protected ChatMessage prepareCallbackData(Event request, ResponseEventData response) {
        String userId = mEngine.chatManager.getChatUser().getUserId();
        ChatMessage responseMessage = parse(response);
        ChatMessage sentMessage = ChatMessage.getSerializer().fromJson(request.getData(), ChatMessage.class);

        if (responseMessage.getBody() != null) {
            // Media message, get download url and update DB
            ChatMediaMessageBody body = (ChatMediaMessageBody)sentMessage.getBody();
            String downloadUrl = ((ChatMediaMessageBody)responseMessage.getBody()).getDownloadUrl();
            body.setDownloadUrl(downloadUrl);

            sentMessage.setBody(body);
        }
        sentMessage.setFrom(userId);
        sentMessage.setEngine(mEngine);

        if (sentMessage != null && mEngine.chatManager.isCacheEnabled()) {
            TSBConversationDataSource dataSource = new TSBConversationDataSource(TSBEngine.getContext(), mEngine);
            dataSource.open();
            dataSource.upsertMessage(userId, sentMessage);
            dataSource.close();
        }

        return sentMessage;
    }

    @Override
    public ChatMessage parse(ResponseEventData response) {
        return ChatMessage.getSerializer().fromJson(response.getResult(), ChatMessage.class);
    }
}
