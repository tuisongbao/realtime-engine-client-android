package com.tuisongbao.engine.chat.group.event;

import com.tuisongbao.engine.chat.group.entity.ChatGroupGetUsersData;
import com.tuisongbao.engine.common.event.BaseRequestEvent;

public class ChatGroupGetUsersEvent extends BaseRequestEvent<ChatGroupGetUsersData> {

    public static final String NAME = "engine_chat:group:getUsers";

    public ChatGroupGetUsersEvent() {
        super(NAME);
    }

}
