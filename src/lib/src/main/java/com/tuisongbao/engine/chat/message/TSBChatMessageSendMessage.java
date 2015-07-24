package com.tuisongbao.engine.chat.message;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.engine.chat.entity.ChatType;
import com.tuisongbao.engine.chat.entity.TSBChatMessageSendData;
import com.tuisongbao.engine.chat.entity.TSBMessage;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageTypeSerializer;
import com.tuisongbao.engine.common.BaseTSBRequestMessage;

public class TSBChatMessageSendMessage extends
        BaseTSBRequestMessage<TSBChatMessageSendData> {

    public static final String NAME = "engine_chat:message:send";

    public TSBChatMessageSendMessage() {
        super(NAME);
    }

    @Override
    protected Gson getSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class,
                new TSBChatMessageChatTypeSerializer());
        gsonBuilder.registerTypeAdapter(TSBMessage.TYPE.class,
                new TSBChatMessageTypeSerializer());
        return gsonBuilder.create();
    }

}
