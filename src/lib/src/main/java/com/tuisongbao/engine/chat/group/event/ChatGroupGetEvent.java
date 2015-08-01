package com.tuisongbao.engine.chat.group.event;

import com.tuisongbao.engine.chat.group.entity.ChatGroupGetData;
import com.tuisongbao.engine.common.event.BaseRequestEvent;

public class ChatGroupGetEvent extends BaseRequestEvent<ChatGroupGetData> {

    public static final String NAME = "engine_chat:group:get";

    public ChatGroupGetEvent() {
        super(NAME);
    }

}
