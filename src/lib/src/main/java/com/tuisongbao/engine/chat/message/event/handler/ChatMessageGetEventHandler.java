package com.tuisongbao.engine.chat.message.event.handler;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageBody;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageBodySerializer;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageTypeSerializer;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

import java.util.List;

public class ChatMessageGetEventHandler extends BaseEventHandler<List<ChatMessage>> {

    @Override
    public List<ChatMessage> parse(ResponseEventData response) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class, new TSBChatMessageChatTypeSerializer());
        gsonBuilder.registerTypeAdapter(ChatMessage.TYPE.class, new TSBChatMessageTypeSerializer());
        gsonBuilder.registerTypeAdapter(ChatMessageBody.class, new TSBChatMessageBodySerializer());
        List<ChatMessage> list = gsonBuilder.create().fromJson(response.getResult(),
                new TypeToken<List<ChatMessage>>() {
                }.getType());
        return list;
    }
}
