package com.tuisongbao.engine.chat.message;

import com.tuisongbao.engine.chat.entity.TSBChatGroupRemoveUserData;
import com.tuisongbao.engine.common.BaseTSBRequestMessage;

public class TSBChatGroupRemoveUserMessage extends BaseTSBRequestMessage<TSBChatGroupRemoveUserData> {

    public static final String NAME = "engine_chat:group:removeUsers";

    public TSBChatGroupRemoveUserMessage() {
        super(NAME);
    }

}
