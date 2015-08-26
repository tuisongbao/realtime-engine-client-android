package com.tuisongbao.engine.chat.group;

import com.tuisongbao.engine.common.event.BaseEvent;

class ChatGroupGetUsersEvent extends BaseEvent<ChatGroupEventData> {

    public static final String NAME = "engine_chat:group:getUsers";

    public ChatGroupGetUsersEvent() {
        super(NAME);
    }

}
