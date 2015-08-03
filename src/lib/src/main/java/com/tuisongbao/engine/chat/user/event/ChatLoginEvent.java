package com.tuisongbao.engine.chat.user.event;

import com.tuisongbao.engine.chat.user.entity.ChatLoginData;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.event.BaseEvent;

public class ChatLoginEvent extends BaseEvent<ChatLoginData> {

    public static final String NAME = "engine_chat:user:login";
    private transient TSBEngineCallback<ChatUser> mCallback;

    public TSBEngineCallback<ChatUser> getCallback() {
        return mCallback;
    }

    public void setCallback(TSBEngineCallback<ChatUser> callback) {
        this.mCallback = callback;
    }

    public ChatLoginEvent() {
        super(NAME);
    }

}
