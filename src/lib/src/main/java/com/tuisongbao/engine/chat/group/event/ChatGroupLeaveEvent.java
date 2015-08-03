package com.tuisongbao.engine.chat.group.event;

import com.tuisongbao.engine.chat.group.entity.ChatGroupEventData;
import com.tuisongbao.engine.common.event.BaseEvent;

public class ChatGroupLeaveEvent extends BaseEvent<ChatGroupEventData> {

    public static final String NAME = "engine_chat:group:leave";

    public ChatGroupLeaveEvent() {
        super(NAME);
    }

}
