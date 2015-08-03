package com.tuisongbao.engine.chat.conversation.event.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.conversation.event.ChatConversationGetEvent;
import com.tuisongbao.engine.chat.db.TSBConversationDataSource;
import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
import com.tuisongbao.engine.chat.conversation.entity.ChatConversationData;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageBody;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageBodySerializer;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageTypeSerializer;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

import java.util.List;

public class ChatConversationGetEventHandler extends BaseEventHandler<List<ChatConversation>> {

    @Override
    protected List<ChatConversation> genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        List<ChatConversation> changedConversations = genCallbackData(request, response);

        TSBConversationDataSource dataSource = new TSBConversationDataSource(TSBEngine.getContext(), mEngine);
        String userId = mEngine.chatManager.getChatUser().getUserId();
        dataSource.open();
        dataSource.upsert(changedConversations, userId);

        ChatConversationData requestData = ((ChatConversationGetEvent)request).getData();
        List<ChatConversation> callbackData = dataSource.getList(userId, requestData.getType(), requestData.getTarget());
        dataSource.close();

        return callbackData;
    }

    @Override
    public List<ChatConversation> genCallbackData(BaseEvent request, RawEvent response) {
        ResponseEventData data = new Gson().fromJson(response.getData(), ResponseEventData.class);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class, new TSBChatMessageChatTypeSerializer());
        gsonBuilder.registerTypeAdapter(ChatMessage.TYPE.class, new TSBChatMessageTypeSerializer());
        gsonBuilder.registerTypeAdapter(ChatMessageBody.class, new TSBChatMessageBodySerializer());
        List<ChatConversation> list = gsonBuilder.create().fromJson(data.getResult(),
                new TypeToken<List<ChatConversation>>() {
                }.getType());
        return list;
    }
}