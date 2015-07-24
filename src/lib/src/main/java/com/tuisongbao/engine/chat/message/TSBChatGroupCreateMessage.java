package com.tuisongbao.engine.chat.message;

import com.tuisongbao.engine.chat.entity.TSBChatGroupCreateData;
import com.tuisongbao.engine.common.BaseTSBRequestMessage;

public class TSBChatGroupCreateMessage extends BaseTSBRequestMessage<TSBChatGroupCreateData> {

    public static final String NAME = "engine_chat:group:create";

    public TSBChatGroupCreateMessage() {
        super(NAME);
    }

}
