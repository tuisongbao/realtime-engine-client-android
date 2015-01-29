package com.tuisongbao.android.engine.chat.message;

import com.tuisongbao.android.engine.chat.entity.TSBChatGroupGetData;
import com.tuisongbao.android.engine.common.BaseTSBRequestMessage;

public class TSBChatGroupGetMessage extends BaseTSBRequestMessage<TSBChatGroupGetData> {

    public static final String NAME = "engine_chat:group:get";

    public TSBChatGroupGetMessage() {
        super(NAME);
    }

}
