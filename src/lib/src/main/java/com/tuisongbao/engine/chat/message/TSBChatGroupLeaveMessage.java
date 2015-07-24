package com.tuisongbao.engine.chat.message;

import com.tuisongbao.engine.chat.entity.TSBChatGroupLeaveData;
import com.tuisongbao.engine.common.BaseTSBRequestMessage;

public class TSBChatGroupLeaveMessage extends BaseTSBRequestMessage<TSBChatGroupLeaveData> {

    public static final String NAME = "engine_chat:group:leave";

    public TSBChatGroupLeaveMessage() {
        super(NAME);
    }

}
