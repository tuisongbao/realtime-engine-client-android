package com.tuisongbao.engine.chat.group.event;

import com.tuisongbao.engine.chat.group.entity.ChatGroupJoinInvitationData;
import com.tuisongbao.engine.common.event.BaseRequestEvent;

public class ChatGroupJoinInvitationEvent extends BaseRequestEvent<ChatGroupJoinInvitationData> {

    public static final String NAME = "engine_chat:group:joinInvitation:send";

    public ChatGroupJoinInvitationEvent() {
        super(NAME);
    }

}
