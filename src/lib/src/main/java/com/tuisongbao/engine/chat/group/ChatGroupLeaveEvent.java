package com.tuisongbao.engine.chat.group;

import com.tuisongbao.engine.common.event.BaseEvent;

class ChatGroupLeaveEvent extends BaseEvent<ChatGroupEventData> {

    public static final String NAME = "engine_chat:group:leave";

    public ChatGroupLeaveEvent() {
        super(NAME);
    }

}
