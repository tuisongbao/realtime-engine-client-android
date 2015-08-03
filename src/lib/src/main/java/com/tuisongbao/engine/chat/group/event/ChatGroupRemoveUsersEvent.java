package com.tuisongbao.engine.chat.group.event;

import com.tuisongbao.engine.chat.group.entity.ChatGroupRemoveUserData;
import com.tuisongbao.engine.common.event.BaseEvent;

public class ChatGroupRemoveUsersEvent extends BaseEvent<ChatGroupRemoveUserData> {

    public static final String NAME = "engine_chat:group:removeUsers";

    public ChatGroupRemoveUsersEvent() {
        super(NAME);
    }

}
