package com.tuisongbao.engine.chat.message;

import com.tuisongbao.engine.chat.entity.TSBChatGroupGetUsersData;
import com.tuisongbao.engine.common.BaseTSBRequestMessage;

public class TSBChatGroupGetUsersMessage extends BaseTSBRequestMessage<TSBChatGroupGetUsersData> {

    public static final String NAME = "engine_chat:group:getUsers";

    public TSBChatGroupGetUsersMessage() {
        super(NAME);
    }

}
