package com.tuisongbao.android.engine.chat.message;

import com.tuisongbao.android.engine.chat.entity.TSBChatGroupJoinInvitationData;
import com.tuisongbao.android.engine.common.BaseTSBRequestMessage;

public class TSBChatGroupJoinInvitationMessage extends BaseTSBRequestMessage<TSBChatGroupJoinInvitationData> {

    public static final String NAME = "engine_chat:group:joinInvitation:send";

    public TSBChatGroupJoinInvitationMessage() {
        super(NAME);
    }

}
