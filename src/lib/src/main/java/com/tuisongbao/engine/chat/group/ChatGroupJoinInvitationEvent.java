package com.tuisongbao.engine.chat.group;

import com.tuisongbao.engine.common.event.BaseEvent;

class ChatGroupJoinInvitationEvent extends BaseEvent<ChatGroupEventData> {

    public static final String NAME = "engine_chat:group:joinInvitation:send";

    public ChatGroupJoinInvitationEvent() {
        super(NAME);
    }

}
