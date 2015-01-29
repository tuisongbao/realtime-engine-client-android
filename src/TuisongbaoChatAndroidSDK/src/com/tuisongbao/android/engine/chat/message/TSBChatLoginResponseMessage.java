package com.tuisongbao.android.engine.chat.message;

import com.google.gson.Gson;
import com.tuisongbao.android.engine.chat.entity.TSBChatUser;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;

public class TSBChatLoginResponseMessage extends BaseTSBResponseMessage<TSBChatUser> {

    @Override
    public TSBChatUser parse() {
        TSBChatUser user = new Gson().fromJson(getData(), TSBChatUser.class);
        return user;
    }

}
