package com.tuisongbao.android.engine.chat.message;

import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;
import com.tuisongbao.android.engine.common.ITSBEngineCallback;
import com.tuisongbao.android.engine.common.TSBEngineCallback;

public class TSBChatMessageResponseMessage extends
        BaseTSBResponseMessage<TSBMessage> {
    private TSBMessage mSendMessage;
    /**
     * 用于保存用户传递的call back
     */
    private TSBEngineCallback<TSBMessage> mCustomCallback;
    
    public TSBChatMessageResponseMessage(TSBChatMessageResponseMessageCallback callback) {
        setCallback(callback);
    }

    public TSBMessage getSendMessage() {
        return mSendMessage;
    }

    public void setSendMessage(TSBMessage sendMessage) {
        this.mSendMessage = sendMessage;
    }

    public TSBEngineCallback<TSBMessage> getCustomCallback() {
        return mCustomCallback;
    }

    public void setCustomCallback(TSBEngineCallback<TSBMessage> customCallback) {
        this.mCustomCallback = customCallback;
    }

    @Override
    public TSBMessage parse() {
        return null;
    }

    @Override
    public void callBack() {
        ((TSBChatMessageResponseMessageCallback)getCallback()).onEvent(this);
    }

    public static interface TSBChatMessageResponseMessageCallback extends ITSBEngineCallback {
        public void onEvent(TSBChatMessageResponseMessage response);
    }

}
