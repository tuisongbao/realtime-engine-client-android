package com.tuisongbao.android.engine.chat.message;

import com.tuisongbao.android.engine.chat.entity.TSBChatLoginData;
import com.tuisongbao.android.engine.chat.entity.TSBChatUser;
import com.tuisongbao.android.engine.common.BaseTSBRequestMessage;
import com.tuisongbao.android.engine.common.TSBEngineCallback;

public class TSBChatLoginMessage extends BaseTSBRequestMessage<TSBChatLoginData> {

    public static final String NAME = "engine_chat:user:login";
    private transient TSBEngineCallback<TSBChatUser> mCallback;

    public TSBEngineCallback<TSBChatUser> getCallback() {
        return mCallback;
    }

    public void setCallback(TSBEngineCallback<TSBChatUser> callback) {
        this.mCallback = callback;
    }

    public TSBChatLoginMessage() {
        super(NAME);
    }

}
