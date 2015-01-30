package com.tuisongbao.android.engine.chat.message;

import org.json.JSONException;
import org.json.JSONObject;

import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;
import com.tuisongbao.android.engine.common.ITSBEngineCallback;
import com.tuisongbao.android.engine.common.TSBEngineBindCallback;
import com.tuisongbao.android.engine.engineio.EngineConstants;

public class TSBChatMessageResponseMessage extends
        BaseTSBResponseMessage<String> {
    private TSBMessage mSendMessage;
    
    public TSBChatMessageResponseMessage(TSBChatMessageResponseMessageCallback callback) {
        setCallback(callback);
    }

    public TSBMessage getSendMessage() {
        return mSendMessage;
    }

    public void setSendMessage(TSBMessage sendMessage) {
        this.mSendMessage = sendMessage;
    }

    @Override
    public String parse() {
        return "";
    }

    @Override
    public void callBack() {
        ((TSBChatMessageResponseMessageCallback)getCallback()).onEvent(this);
        if (isSuccess()) {
            if (TSBChatMessageGetMessage.NAME.equals(getBindName())) {
                // 当为获取消息时
                try {
                    JSONObject json = new JSONObject();
                    json.put(EngineConstants.REQUEST_KEY_RESPONSE_TO, getServerRequestId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (TSBChatMessageSendMessage.NAME.equals(getBindName())) {
                // 发送消息成功
                ((TSBEngineBindCallback)getCallback()).onEvent(getBindName(), getName(), getData());
            } else if (EngineConstants.CHAT_NAME_NEW_MESSAGE.equals(getBindName())) {
                // 接收到消息
                try {
                    JSONObject json = new JSONObject();
                    json.put(EngineConstants.REQUEST_KEY_RESPONSE_TO, getServerRequestId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ((TSBEngineBindCallback)getCallback()).onEvent(getBindName(), getName(), getData());
            }
        } else {
            // 发送消息失败
            if (TSBChatMessageSendMessage.NAME.equals(getBindName())) {
                ((TSBEngineBindCallback)getCallback()).onEvent(getBindName(), getName(), getData());
            }
        }
    }

    public static interface TSBChatMessageResponseMessageCallback extends ITSBEngineCallback {
        public void onEvent(TSBChatMessageResponseMessage response);
    }

}
