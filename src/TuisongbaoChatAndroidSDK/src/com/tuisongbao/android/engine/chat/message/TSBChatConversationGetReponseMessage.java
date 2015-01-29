package com.tuisongbao.android.engine.chat.message;

import com.google.gson.Gson;
import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;

public class TSBChatConversationGetReponseMessage extends
        BaseTSBResponseMessage<TSBChatConversation> {

    @Override
    public TSBChatConversation parse() {
        TSBChatConversation conversation = new Gson().fromJson(getData(),
                TSBChatConversation.class);
        return conversation;
    }

}
