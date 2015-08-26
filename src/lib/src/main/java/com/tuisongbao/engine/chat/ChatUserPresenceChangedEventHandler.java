package com.tuisongbao.engine.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.serializer.ChatUserPresenceSerializer;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.BaseEventHandler;

class ChatUserPresenceChangedEventHandler extends BaseEventHandler<ChatUserPresence> {

    public ChatUserPresenceChangedEventHandler(Engine engine) {
        setEngine(engine);
    }

    @Override
    protected ChatUserPresence genCallbackData(BaseEvent request, RawEvent response) {
        Gson gson = new GsonBuilder().registerTypeAdapter(ChatUserPresence.Presence.class,
                new ChatUserPresenceSerializer()).create();
        return gson.fromJson(response.getData(), ChatUserPresence.class);
    }

    @Override
    public void onResponse(BaseEvent request, RawEvent response) {
        engine.getChatManager().trigger(ChatManager.EVENT_PRESENCE_CHANGED, genCallbackData(request, response));
    }
}