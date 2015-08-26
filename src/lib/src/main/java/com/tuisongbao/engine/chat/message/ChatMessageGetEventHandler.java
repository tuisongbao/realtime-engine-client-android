package com.tuisongbao.engine.chat.message;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.BaseEventHandler;

import java.util.List;

class ChatMessageGetEventHandler extends BaseEventHandler<List<ChatMessage>> {

    @Override
    public List<ChatMessage> genCallbackData(BaseEvent request, RawEvent response) {
        ResponseEventData data = new Gson().fromJson(response.getData(), ResponseEventData.class);
        List<ChatMessage> list = ChatMessage.getSerializer().fromJson(data.getResult(),
                new TypeToken<List<ChatMessage>>() {
                }.getType());
        return list;
    }
}