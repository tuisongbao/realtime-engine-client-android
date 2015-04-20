package com.tuisongbao.android.engine.chat.message;

import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.db.TSBConversationDataSource;
import com.tuisongbao.android.engine.chat.entity.ChatType;
import com.tuisongbao.android.engine.chat.entity.TSBChatMessageGetData;
import com.tuisongbao.android.engine.chat.entity.TSBChatUser;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.android.engine.log.LogUtil;

public class TSBChatMessageMultiGetResponseMessage extends TSBChatMessageGetResponseMessage {
    private int requestCount = 0;
    private Long startMessageId;
    private Long endMessageId;

    public int getRequestCount() {
        return requestCount;
    }

    public void incRequestCount() {
        LogUtil.debug(LogUtil.LOG_TAG_CHAT_CACHE, this + " already has " + requestCount + " requests");
        this.requestCount++;
    }

    public void setMessageIdSpan(Long startMessageId, Long endMessageId) {
        this.startMessageId = startMessageId;
        this.endMessageId = endMessageId;
    }


    @Override
    protected List<TSBMessage> prepareCallBackData() {
        List<TSBMessage> messages = super.prepareCallBackData();
        TSBChatUser user = TSBChatManager.getInstance().getChatUser();
        TSBConversationDataSource dataSource = new TSBConversationDataSource(TSBEngine.getContext());
        dataSource.open();
        for (TSBMessage message : messages) {
            dataSource.upsertMessage(user.getUserId(), message);
        }

        TSBChatMessageGetData requestData = parseRequestData();
        messages = dataSource.getMessages(requestData.getType(), requestData.getTarget(), startMessageId, endMessageId
                , requestData.getLimit());
        dataSource.close();

        return messages;
    }


    @Override
    public void callBack() {
        requestCount--;
        prepareCallBackData();
        LogUtil.debug(LogUtil.LOG_TAG_CHAT_CACHE, this + " remain " + requestCount + " requests");
        if (requestCount < 1) {
            super.callBack();
        }
    }

    private TSBChatMessageGetData parseRequestData() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class, new TSBChatMessageChatTypeSerializer());
        TSBChatMessageGetData requestData = gsonBuilder.create().fromJson((String)getRequestData(),
                new TypeToken<TSBChatMessageGetData>() {
                }.getType());

        return requestData;
    }
}
