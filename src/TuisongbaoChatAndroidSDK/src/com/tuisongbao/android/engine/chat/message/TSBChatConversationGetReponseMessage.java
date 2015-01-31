package com.tuisongbao.android.engine.chat.message;

import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.android.engine.chat.entity.ChatType;
import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;

public class TSBChatConversationGetReponseMessage extends
        BaseTSBResponseMessage<List<TSBChatConversation>> {

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
