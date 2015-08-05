package com.tuisongbao.engine.chat.user.event;

import com.tuisongbao.engine.chat.user.entity.ChatLoginData;
import com.tuisongbao.engine.common.event.BaseEvent;

public class ChatLoginEvent extends BaseEvent<ChatLoginData> {

    public ChatLoginEvent() {
        super("engine_chat:user:login");
    }
}
