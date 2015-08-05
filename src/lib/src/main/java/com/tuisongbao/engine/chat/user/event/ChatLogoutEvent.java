package com.tuisongbao.engine.chat.user.event;

import com.tuisongbao.engine.common.event.BaseEvent;

public class ChatLogoutEvent extends BaseEvent<String> {

    public ChatLogoutEvent() {
        super("engine_chat:user:logout");
    }
}
