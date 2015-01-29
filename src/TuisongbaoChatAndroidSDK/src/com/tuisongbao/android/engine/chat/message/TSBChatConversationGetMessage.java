package com.tuisongbao.android.engine.chat.message;

import com.tuisongbao.android.engine.chat.entity.TSBChatConversationData;
import com.tuisongbao.android.engine.common.BaseTSBRequestMessage;

public class TSBChatConversationGetMessage extends BaseTSBRequestMessage<TSBChatConversationData> {

    public static final String NAME = "engine_chat:conversation:get";

    public TSBChatConversationGetMessage() {
        super(NAME);
    }

}
