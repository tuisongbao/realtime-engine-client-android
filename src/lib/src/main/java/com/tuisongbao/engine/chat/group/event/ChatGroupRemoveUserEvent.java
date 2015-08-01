package com.tuisongbao.engine.chat.group.event;

import com.tuisongbao.engine.chat.group.entity.ChatGroupRemoveUserData;
import com.tuisongbao.engine.common.event.BaseRequestEvent;

public class ChatGroupRemoveUserEvent extends BaseRequestEvent<ChatGroupRemoveUserData> {

    public static final String NAME = "engine_chat:group:removeUsers";

    public ChatGroupRemoveUserEvent() {
        super(NAME);
    }

}
