package com.tuisongbao.engine.chat.message.event.handler;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.db.TSBConversationDataSource;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageGetData;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.common.entity.Event;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.log.LogUtil;

import java.util.List;

public class ChatMessageMultiGetEventHandler extends ChatMessageGetEventHandler {
    private int requestCount = 0;
    private Long startMessageId;
    private Long endMessageId;

    public int getRequestCount() {
        return requestCount;
    }

    public void incRequestCount() {
        this.requestCount++;
        LogUtil.debug(LogUtil.LOG_TAG_CHAT_CACHE, this + " has " + requestCount + " requests");
    }

    public void setMessageIdSpan(Long startMessageId, Long endMessageId) {
        this.startMessageId = startMessageId;
        this.endMessageId = endMessageId;
    }

    @Override
    protected List<ChatMessage> prepareCallbackData(Event request, ResponseEventData response) {
        List<ChatMessage> messages = parse(response);
        ChatUser user = mEngine.chatManager.getChatUser();
        TSBConversationDataSource dataSource = new TSBConversationDataSource(TSBEngine.getContext(), mEngine);
        dataSource.open();
        for (ChatMessage message : messages) {
            dataSource.upsertMessage(user.getUserId(), message);
        }

        ChatMessageGetData requestData = parseRequestData(request);
        messages = dataSource.getMessages(user.getUserId(), requestData.getType(), requestData.getTarget(), startMessageId, endMessageId
                , requestData.getLimit());
        dataSource.close();

        return messages;
    }


    @Override
    public void callback(Event request, ResponseEventData response) {
        requestCount--;
        prepareCallbackData(request, response);
        LogUtil.debug(LogUtil.LOG_TAG_CHAT_CACHE, this + " remain " + requestCount + " requests");
        if (requestCount < 1) {
            super.callback(request, response);
        }
    }

    private ChatMessageGetData parseRequestData(Event request) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class, new TSBChatMessageChatTypeSerializer());
        ChatMessageGetData requestData = gsonBuilder.create().fromJson(request.getData(),
                new TypeToken<ChatMessageGetData>() {
                }.getType());

        return requestData;
    }
}
