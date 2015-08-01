package com.tuisongbao.engine.chat.user.event;

import com.tuisongbao.engine.common.event.BaseRequestEvent;

public class ChatLogoutEvent extends BaseRequestEvent<String> {

    public static final String NAME = "engine_chat:user:logout";

    public ChatLogoutEvent() {
        super(NAME);
    }

}
