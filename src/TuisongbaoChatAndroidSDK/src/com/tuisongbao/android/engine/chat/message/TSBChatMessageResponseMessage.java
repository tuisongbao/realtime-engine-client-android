package com.tuisongbao.android.engine.chat.message;

import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.db.TSBConversationDataSource;
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

    public void runPreCallBack(TSBMessage message) {
        preCallBack(message);
    }

    @Override
    public TSBMessage parse() {
        return null;
    }

    @Override
    protected void preCallBack(TSBMessage data) {
        super.preCallBack(data);

        if (!TSBChatManager.getInstance().isUseCache()) {
            return;
        }

        TSBConversationDataSource dataSource = new TSBConversationDataSource(TSBEngine.getContext());
        dataSource.open();
        dataSource.addMessage(TSBChatManager.getInstance().getChatUser().getUserId(), data);
        dataSource.close();
    }

    @Override
    public void callBack() {
        ((TSBChatMessageResponseMessageCallback)getCallback()).onEvent(this);
    }

    public static interface TSBChatMessageResponseMessageCallback extends ITSBEngineCallback {
        public void onEvent(TSBChatMessageResponseMessage response);
    }

}
