package com.tuisongbao.android.engine.chat.message;

import com.tuisongbao.android.engine.chat.entity.TSBChatGroupCreateData;
import com.tuisongbao.android.engine.common.BaseTSBRequestMessage;

public class TSBChatGroupCreateMessage extends BaseTSBRequestMessage<TSBChatGroupCreateData> {

    public static final String NAME = "engine_chat:group:create";

    public TSBChatGroupCreateMessage() {
        super(NAME);
    }

}
