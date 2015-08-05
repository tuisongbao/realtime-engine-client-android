package com.tuisongbao.engine.chat.message.event.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageContent;
import com.tuisongbao.engine.chat.serializer.ChatMessageChatTypeSerializer;
import com.tuisongbao.engine.chat.serializer.ChatMessageContentSerializer;
import com.tuisongbao.engine.chat.serializer.ChatMessageTypeSerializer;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

import java.util.List;

public class ChatMessageGetEventHandler extends BaseEventHandler<List<ChatMessage>> {

    @Override
    public List<ChatMessage> genCallbackData(BaseEvent request, RawEvent response) {
        ResponseEventData data = new Gson().fromJson(response.getData(), ResponseEventData.class);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class, new ChatMessageChatTypeSerializer());
        gsonBuilder.registerTypeAdapter(ChatMessage.TYPE.class, new ChatMessageTypeSerializer());
        gsonBuilder.registerTypeAdapter(ChatMessageContent.class, new ChatMessageContentSerializer());
        List<ChatMessage> list = gsonBuilder.create().fromJson(data.getResult(),
                new TypeToken<List<ChatMessage>>() {
                }.getType());
        return list;
    }
}
