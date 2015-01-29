package com.tuisongbao.android.engine.chat.message;

import com.tuisongbao.android.engine.common.BaseTSBRequestMessage;
import com.tuisongbao.android.engine.common.TSBEngineCallback;

public class TSBChatLogoutMessage extends BaseTSBRequestMessage<String> {

    public static final String NAME = "engine_chat:user:logout";
    private transient TSBEngineCallback<String> mCallback;

    public TSBEngineCallback<String> getCallback() {
        return mCallback;
    }

    public void setCallback(TSBEngineCallback<String> canllback) {
        this.mCallback = canllback;
    }

    public TSBChatLogoutMessage() {
        super(NAME);
    }

}
