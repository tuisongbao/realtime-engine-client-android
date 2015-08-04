package com.tuisongbao.engine.chat.user.event;

import com.tuisongbao.engine.chat.user.entity.ChatLoginData;
import com.tuisongbao.engine.common.event.BaseEvent;

public class ChatLoginEvent extends BaseEvent<ChatLoginData> {

    public static final String NAME = "engine_chat:user:login";

    public ChatLoginEvent() {
        super(NAME);
    }
}
