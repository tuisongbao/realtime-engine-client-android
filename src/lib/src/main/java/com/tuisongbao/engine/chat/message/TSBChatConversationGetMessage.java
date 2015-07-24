package com.tuisongbao.engine.chat.message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.engine.chat.entity.ChatType;
import com.tuisongbao.engine.chat.entity.TSBChatConversationData;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.engine.common.BaseTSBRequestMessage;

public class TSBChatConversationGetMessage extends BaseTSBRequestMessage<TSBChatConversationData> {

    public static final String NAME = "engine_chat:conversation:get";

    public TSBChatConversationGetMessage() {
        super(NAME);
    }

    @Override
    protected Gson getSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class, new TSBChatMessageChatTypeSerializer());
        return gsonBuilder.create();
    }

}
