package com.tuisongbao.engine.chat.group.event;

import com.tuisongbao.engine.chat.group.entity.ChatGroupEventData;
import com.tuisongbao.engine.common.event.BaseEvent;

public class ChatGroupGetEvent extends BaseEvent<ChatGroupEventData> {

    public static final String NAME = "engine_chat:group:get";

    public ChatGroupGetEvent() {
        super(NAME);
    }

}
