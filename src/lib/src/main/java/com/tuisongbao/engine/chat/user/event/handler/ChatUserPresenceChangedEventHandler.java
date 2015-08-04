package com.tuisongbao.engine.chat.user.event.handler;

import com.google.gson.Gson;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.user.entity.ChatUserPresenceData;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

/**
 * Created by root on 15-8-3.
 */
public class ChatUserPresenceChangedEventHandler extends BaseEventHandler<ChatUserPresenceData> {
    @Override
    protected ChatUserPresenceData genCallbackData(BaseEvent request, RawEvent response) {
        return new Gson().fromJson(response.getData(), ChatUserPresenceData.class);
    }

    @Override
    public void onResponse(BaseEvent request, RawEvent response) {
        engine.getChatManager().trigger(ChatManager.EVENT_PRESENCE_CHANGED, genCallbackData(request, response));
    }
}
