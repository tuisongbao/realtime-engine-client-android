package com.tuisongbao.engine.chat;

import com.tuisongbao.engine.common.event.BaseEvent;

class ChatLoginEvent extends BaseEvent<ChatLoginData> {

    public ChatLoginEvent() {
        super("engine_chat:user:login");
    }
}
