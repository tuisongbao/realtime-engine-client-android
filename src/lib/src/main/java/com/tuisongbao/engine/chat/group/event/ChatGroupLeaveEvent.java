package com.tuisongbao.engine.chat.group.event;

import com.tuisongbao.engine.chat.group.entity.ChatGroupLeaveData;
import com.tuisongbao.engine.common.event.BaseEvent;

public class ChatGroupLeaveEvent extends BaseEvent<ChatGroupLeaveData> {

    public static final String NAME = "engine_chat:group:leave";

    public ChatGroupLeaveEvent() {
        super(NAME);
    }

}
