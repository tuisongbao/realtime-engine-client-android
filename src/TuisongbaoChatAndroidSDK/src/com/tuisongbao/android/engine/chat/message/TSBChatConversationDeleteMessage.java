package com.tuisongbao.android.engine.chat.message;

import com.tuisongbao.android.engine.chat.entity.TSBChatConversationData;
import com.tuisongbao.android.engine.common.BaseTSBRequestMessage;

public class TSBChatConversationDeleteMessage extends BaseTSBRequestMessage<TSBChatConversationData> {

    public static final String NAME = "engine_chat:conversation:delete";

    public TSBChatConversationDeleteMessage() {
        super(NAME);
    }

}
