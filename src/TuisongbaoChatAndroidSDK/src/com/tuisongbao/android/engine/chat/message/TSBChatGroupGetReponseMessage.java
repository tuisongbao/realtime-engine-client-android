package com.tuisongbao.android.engine.chat.message;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;

public class TSBChatGroupGetReponseMessage extends
        BaseTSBResponseMessage<List<TSBChatGroup>> {

    @Override
    public List<TSBChatGroup> parse() {
        List<TSBChatGroup> list = new Gson().fromJson(getData(),
                new TypeToken<List<TSBChatGroup>>() {
                }.getType());
        return list;
    }

}
