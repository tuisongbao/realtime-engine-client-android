package com.tuisongbao.engine.chat.user.event;

import com.tuisongbao.engine.chat.user.entity.ChatLoginData;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.common.event.BaseRequestEvent;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;

public class ChatLoginEvent extends BaseRequestEvent<ChatLoginData> {

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
