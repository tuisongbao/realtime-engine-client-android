package com.tuisongbao.engine.chat.group.event;

import com.tuisongbao.engine.chat.group.entity.ChatGroupLeaveData;
import com.tuisongbao.engine.common.event.BaseRequestEvent;

public class ChatGroupLeaveEvent extends BaseRequestEvent<ChatGroupLeaveData> {

    public static final String NAME = "engine_chat:group:leave";

    public ChatGroupLeaveEvent() {
        super(NAME);
    }

}
