package com.tuisongbao.engine.chat.user.event.handler;

import com.google.gson.Gson;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

public class ChatLoginEventHandler extends BaseEventHandler<ChatUser> {

    @Override
    public ChatUser parse(ResponseEventData response) {
        ChatUser user = new Gson().fromJson(response.getResult(), ChatUser.class);
        return user;
    }

}
