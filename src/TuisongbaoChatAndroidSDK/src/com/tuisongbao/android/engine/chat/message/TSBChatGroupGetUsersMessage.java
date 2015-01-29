package com.tuisongbao.android.engine.chat.message;

import com.tuisongbao.android.engine.chat.entity.TSBChatGroupGetUsersData;
import com.tuisongbao.android.engine.common.BaseTSBRequestMessage;

public class TSBChatGroupGetUsersMessage extends BaseTSBRequestMessage<TSBChatGroupGetUsersData> {

    public static final String NAME = "engine_chat:group:getUsers";

    public TSBChatGroupGetUsersMessage() {
        super(NAME);
    }

}
