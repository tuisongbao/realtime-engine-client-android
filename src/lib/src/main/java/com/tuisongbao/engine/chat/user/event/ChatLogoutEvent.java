package com.tuisongbao.engine.chat.user.event;

import com.tuisongbao.engine.common.event.BaseEvent;

public class ChatLogoutEvent extends BaseEvent<String> {

    public static final String NAME = "engine_chat:user:logout";

    public ChatLogoutEvent() {
        super(NAME);
    }

}
