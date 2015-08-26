package com.tuisongbao.engine.chat.group;

import com.tuisongbao.engine.common.event.BaseEvent;

class ChatGroupRemoveUsersEvent extends BaseEvent<ChatGroupEventData> {

    public static final String NAME = "engine_chat:group:removeUsers";

    public ChatGroupRemoveUsersEvent() {
        super(NAME);
    }

}
