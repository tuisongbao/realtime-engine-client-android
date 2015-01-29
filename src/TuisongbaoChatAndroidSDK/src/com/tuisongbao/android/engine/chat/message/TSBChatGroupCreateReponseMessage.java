package com.tuisongbao.android.engine.chat.message;

import com.google.gson.Gson;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;

public class TSBChatGroupCreateReponseMessage extends
        BaseTSBResponseMessage<TSBChatGroup> {

    @Override
    public TSBChatGroup parse() {
        TSBChatGroup group = new Gson().fromJson(getData(),
                TSBChatGroup.class);
        return group;
    }

}
