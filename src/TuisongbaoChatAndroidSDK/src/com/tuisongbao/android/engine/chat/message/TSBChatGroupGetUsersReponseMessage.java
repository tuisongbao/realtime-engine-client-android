package com.tuisongbao.android.engine.chat.message;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupUser;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;

public class TSBChatGroupGetUsersReponseMessage extends
        BaseTSBResponseMessage<List<TSBChatGroupUser>> {

    @Override
    public List<TSBChatGroupUser> parse() {
        List<TSBChatGroupUser> list = new Gson().fromJson(getData(),
                new TypeToken<List<TSBChatGroupUser>>() {
                }.getType());
        return list;
    }

}
