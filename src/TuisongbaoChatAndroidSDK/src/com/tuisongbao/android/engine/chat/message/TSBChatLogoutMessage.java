package com.tuisongbao.android.engine.chat.message;

import com.tuisongbao.android.engine.common.BaseTSBRequestMessage;

public class TSBChatLogoutMessage extends BaseTSBRequestMessage<String> {

    public static final String NAME = "engine_chat:user:logout";

    public TSBChatLogoutMessage() {
        super(NAME);
    }

}
