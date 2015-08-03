package com.tuisongbao.engine.chat.group.event;

import com.tuisongbao.engine.chat.group.entity.ChatGroupGetUsersData;
import com.tuisongbao.engine.common.event.BaseEvent;

public class ChatGroupGetUsersEvent extends BaseEvent<ChatGroupGetUsersData> {

    public static final String NAME = "engine_chat:group:getUsers";

    public ChatGroupGetUsersEvent() {
        super(NAME);
    }

}
