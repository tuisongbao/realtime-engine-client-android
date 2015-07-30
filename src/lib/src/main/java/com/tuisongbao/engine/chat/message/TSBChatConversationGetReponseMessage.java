package com.tuisongbao.engine.chat.message;

import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.TSBChatManager;
import com.tuisongbao.engine.chat.db.TSBConversationDataSource;
import com.tuisongbao.engine.chat.entity.ChatType;
import com.tuisongbao.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.engine.chat.entity.TSBChatConversationData;
import com.tuisongbao.engine.chat.entity.TSBMessage;
import com.tuisongbao.engine.chat.entity.TSBMessageBody;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageBodySerializer;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageTypeSerializer;
import com.tuisongbao.engine.common.BaseTSBResponseMessage;

public class TSBChatConversationGetReponseMessage extends
        BaseTSBResponseMessage<List<TSBChatConversation>> {

    @Override
    protected List<TSBChatConversation> prepareCallBackData() {
        List<TSBChatConversation> changedConversations = super.prepareCallBackData();

        if (!mEngine.chatManager.isCacheEnabled()) {
            return changedConversations;
        }

        TSBConversationDataSource dataSource = new TSBConversationDataSource(TSBEngine.getContext(), mEngine.chatManager);
        String userId = mEngine.chatManager.getChatUser().getUserId();
        dataSource.open();
        dataSource.upsert(changedConversations, userId);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class, new TSBChatMessageChatTypeSerializer());
        TSBChatConversationData requestData = gsonBuilder.create().fromJson((String)getRequestData(),
                new TypeToken<TSBChatConversationData>() {
                }.getType());

        List<TSBChatConversation> callbackData = dataSource.getList(userId, requestData.getType(), requestData.getTarget());
        dataSource.close();

        return callbackData;
    }

    @Override
    public List<TSBChatConversation> parse() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class, new TSBChatMessageChatTypeSerializer());
        gsonBuilder.registerTypeAdapter(TSBMessage.TYPE.class, new TSBChatMessageTypeSerializer());
        gsonBuilder.registerTypeAdapter(TSBMessageBody.class, new TSBChatMessageBodySerializer());
        List<TSBChatConversation> list = gsonBuilder.create().fromJson(getData(),
                new TypeToken<List<TSBChatConversation>>() {
                }.getType());
        return list;
    }
}
