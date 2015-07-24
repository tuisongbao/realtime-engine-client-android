package com.tuisongbao.engine.chat.message;

import com.tuisongbao.engine.chat.entity.TSBChatGroupGetData;
import com.tuisongbao.engine.common.BaseTSBRequestMessage;

public class TSBChatGroupGetMessage extends BaseTSBRequestMessage<TSBChatGroupGetData> {

    public static final String NAME = "engine_chat:group:get";

    public TSBChatGroupGetMessage() {
        super(NAME);
    }

}
