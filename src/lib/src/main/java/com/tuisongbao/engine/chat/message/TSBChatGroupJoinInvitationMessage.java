package com.tuisongbao.engine.chat.message;

import com.tuisongbao.engine.chat.entity.TSBChatGroupJoinInvitationData;
import com.tuisongbao.engine.common.BaseTSBRequestMessage;

public class TSBChatGroupJoinInvitationMessage extends BaseTSBRequestMessage<TSBChatGroupJoinInvitationData> {

    public static final String NAME = "engine_chat:group:joinInvitation:send";

    public TSBChatGroupJoinInvitationMessage() {
        super(NAME);
    }

}
