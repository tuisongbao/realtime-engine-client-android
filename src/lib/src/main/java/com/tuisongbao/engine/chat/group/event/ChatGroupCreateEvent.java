package com.tuisongbao.engine.chat.group.event;

import com.tuisongbao.engine.chat.group.entity.ChatGroupCreateData;
import com.tuisongbao.engine.common.event.BaseEvent;

public class ChatGroupCreateEvent extends BaseEvent<ChatGroupCreateData> {

    public static final String NAME = "engine_chat:group:create";

    public ChatGroupCreateEvent() {
        super(NAME);
    }

}
