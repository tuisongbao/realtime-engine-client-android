package com.tuisongbao.engine.chat.message.event.handler;

import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.db.ChatConversationDataSource;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageGetData;
import com.tuisongbao.engine.chat.message.event.ChatMessageGetEvent;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.utils.LogUtils;

import java.util.List;

public class ChatMessageMultiGetEventHandler extends ChatMessageGetEventHandler {
    private final String TAG = "TSB" + ChatMessageMultiGetEventHandler.class.getSimpleName();

    private int requestCount = 0;
    private Long startMessageId;
    private Long endMessageId;

    public int getRequestCount() {
        return requestCount;
    }

    public void incRequestCount() {
        this.requestCount++;
        LogUtils.debug(TAG, this + " has " + requestCount + " requests");
    }

    public void setMessageIdSpan(Long startMessageId, Long endMessageId) {
        this.startMessageId = startMessageId;
        this.endMessageId = endMessageId;
    }

    @Override
    protected List<ChatMessage> genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        List<ChatMessage> messages = genCallbackData(request, response);
        ChatUser user = engine.getChatManager().getChatUser();
        ChatConversationDataSource dataSource = new ChatConversationDataSource(Engine.getContext(), engine);
        dataSource.open();
        for (ChatMessage message : messages) {
            dataSource.upsertMessage(user.getUserId(), message);
            message.setEngine(engine);
        }

        ChatMessageGetData requestData = ((ChatMessageGetEvent)request).getData();
        messages = dataSource.getMessages(user.getUserId(), requestData.getType(), requestData.getTarget(), startMessageId, endMessageId
                , requestData.getLimit());
        dataSource.close();

        return messages;
    }


    @Override
    public void onResponse(BaseEvent request, RawEvent response) {
        requestCount--;
        genCallbackDataWithCache(request, response);
        LogUtils.debug(TAG, this + " remain " + requestCount + " requests");
        if (requestCount < 1) {
            super.onResponse(request, response);
        }
    }
}
