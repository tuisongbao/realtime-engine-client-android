package com.tuisongbao.android.engine.chat.message;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;

public class TSBChatConversationGetReponseMessage extends
        BaseTSBResponseMessage<List<TSBChatConversation>> {

    @Override
    public List<TSBChatConversation> parse() {
        List<TSBChatConversation> list = new Gson().fromJson(getData(),
                new TypeToken<List<TSBChatConversation>>() {
                }.getType());
        return list;
    }

}
