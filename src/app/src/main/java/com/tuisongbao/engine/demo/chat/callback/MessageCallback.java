package com.tuisongbao.engine.demo.chat.callback;

import com.google.gson.JsonObject;
import com.tuisongbao.engine.chat.message.ChatMessage;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.bean.MessageStatus;

/**
 * Created by user on 15-9-2.
 */
public class MessageCallback implements EngineCallback<ChatMessage>{
    private ChatMessage sendingMessage;

    public MessageCallback(final ChatMessage sendingMessage) {
        this.sendingMessage = sendingMessage;
    }

    @Override
    public void onSuccess(ChatMessage chatMessage) {
        this.sendingMessage.setContent(chatMessage.getContent());
        JsonObject json;
        if(sendingMessage.getContent().getExtra() == null){
            json = new JsonObject();
        }else{
            json = sendingMessage.getContent().getExtra();
        }
        json.addProperty("status", MessageStatus.SUCCESS.getValue());
    }

    @Override
    public void onError(ResponseError error) {
        JsonObject json;
        if(sendingMessage.getContent().getExtra() == null){
            json = new JsonObject();
        }else{
            json = sendingMessage.getContent().getExtra();
        }
        json.addProperty("status", MessageStatus.FAIL.getValue());
    }
}
