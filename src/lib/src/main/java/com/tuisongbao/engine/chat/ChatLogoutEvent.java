package com.tuisongbao.engine.chat;

import com.tuisongbao.engine.common.event.BaseEvent;

class ChatLogoutEvent extends BaseEvent<String> {

    public ChatLogoutEvent() {
        super("engine_chat:user:logout");
    }
}
