package com.tuisongbao.android.engine.chat.message;

import com.tuisongbao.android.engine.chat.entity.TSBChatGroupLeaveData;
import com.tuisongbao.android.engine.common.BaseTSBRequestMessage;

public class TSBChatGroupLeaveMessage extends BaseTSBRequestMessage<TSBChatGroupLeaveData> {

    public static final String NAME = "engine_chat:group:leave";

    public TSBChatGroupLeaveMessage() {
        super(NAME);
    }

}
