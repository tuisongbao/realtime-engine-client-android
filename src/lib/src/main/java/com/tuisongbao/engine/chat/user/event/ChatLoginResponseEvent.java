package com.tuisongbao.engine.chat.user.event;

import com.google.gson.Gson;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.common.event.BaseResponseEvent;

public class ChatLoginResponseEvent extends BaseResponseEvent<ChatUser> {

    @Override
    public ChatUser parse() {
        ChatUser user = new Gson().fromJson(getData(), ChatUser.class);
        return user;
    }

}
