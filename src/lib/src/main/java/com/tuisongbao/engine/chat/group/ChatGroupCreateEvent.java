package com.tuisongbao.engine.chat.group;

import com.tuisongbao.engine.common.event.BaseEvent;

class ChatGroupCreateEvent extends BaseEvent<ChatGroupEventData> {

    public static final String NAME = "engine_chat:group:create";

    public ChatGroupCreateEvent() {
        super(NAME);
    }

}
