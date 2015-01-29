package com.tuisongbao.android.engine.chat.message;

import com.tuisongbao.android.engine.chat.entity.TSBChatGroupRemoveUserData;
import com.tuisongbao.android.engine.common.BaseTSBRequestMessage;

public class TSBChatGroupRemoveUserMessage extends BaseTSBRequestMessage<TSBChatGroupRemoveUserData> {

    public static final String NAME = "engine_chat:group:removeUser";

    public TSBChatGroupRemoveUserMessage() {
        super(NAME);
    }

}
