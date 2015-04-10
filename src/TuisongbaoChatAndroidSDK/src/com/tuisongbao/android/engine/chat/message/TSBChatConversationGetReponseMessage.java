package com.tuisongbao.android.engine.chat.message;

import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.db.TSBConversationDataSource;
import com.tuisongbao.android.engine.chat.entity.ChatType;
import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;

public class TSBChatConversationGetReponseMessage extends
        BaseTSBResponseMessage<List<TSBChatConversation>> {

    @Override
    protected void preCallBack(List<TSBChatConversation> conversations) {
        super.preCallBack(conversations);

        if (!TSBChatManager.getInstance().isUseCache()) {
            return;
        }

        TSBConversationDataSource dataSource = new TSBConversationDataSource(TSBEngine.getContext());
        dataSource.open();
        for (TSBChatConversation conversation : conversations) {
            dataSource.insert(conversation, TSBChatManager.getInstance().getChatUser().getUserId());
        }
        dataSource.close();
    }

    @Override
    public List<TSBChatConversation> parse() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class, new TSBChatMessageChatTypeSerializer());
        List<TSBChatConversation> list = gsonBuilder.create().fromJson(getData(),
                new TypeToken<List<TSBChatConversation>>() {
                }.getType());
        return list;
    }

}
