package com.tuisongbao.engine.chat.conversation.event.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
import com.tuisongbao.engine.chat.conversation.event.ChatConversationGetEvent;
import com.tuisongbao.engine.chat.db.ChatConversationDataSource;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

import java.util.List;

public class ChatConversationGetEventHandler extends BaseEventHandler<List<ChatConversation>> {

    @Override
    protected List<ChatConversation> genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        List<ChatConversation> changedConversations = genCallbackData(request, response);

        ChatConversationDataSource dataSource = new ChatConversationDataSource(Engine.getContext(), engine);
        String userId = engine.getChatManager().getChatUser().getUserId();
        dataSource.open();
        dataSource.upsert(changedConversations, userId);

        ChatConversation requestData = ((ChatConversationGetEvent)request).getData();
        List<ChatConversation> callbackData = dataSource.getList(userId, requestData.getType(), requestData.getTarget());
        dataSource.close();

        return callbackData;
    }

    @Override
    public List<ChatConversation> genCallbackData(BaseEvent request, RawEvent response) {
        ResponseEventData data = new Gson().fromJson(response.getData(), ResponseEventData.class);

        Gson gson = ChatMessage.getSerializer();
        List<ChatConversation> list = gson.fromJson(data.getResult(),
                new TypeToken<List<ChatConversation>>() {
                }.getType());
        return list;
    }
}
