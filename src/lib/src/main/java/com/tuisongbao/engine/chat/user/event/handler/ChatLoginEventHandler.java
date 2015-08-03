package com.tuisongbao.engine.chat.user.event.handler;

import com.google.gson.Gson;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.user.entity.ChatLoginData;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.chat.user.event.ChatLoginEvent;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

public class ChatLoginEventHandler extends BaseEventHandler<ChatUser> {

    @Override
    public ChatUser genCallbackData(BaseEvent request, RawEvent response) {
        Gson gson = new Gson();
        ResponseEventData data = gson.fromJson(response.getData(), ResponseEventData.class);
        ChatUser user = new Gson().fromJson(data.getResult(), ChatUser.class);
        return user;
    }

    @Override
    public void onResponse(BaseEvent request, RawEvent response) {
        Gson gson = new Gson();
        ResponseEventData responseData = gson.fromJson(response.getData(), ResponseEventData.class);
        TSBEngineCallback callback = (TSBEngineCallback)getCallback();

        if (responseData.getOk()) {
            ChatLoginData data = ((ChatLoginEvent)request).getData();
            ChatManager chatManager = mEngine.chatManager;

            // TODO: 15-8-3 Test, is it work ?
            ChatUser userDataInRequest = new Gson().fromJson(data.getUserData(), ChatUser.class);
            ChatUser user = genCallbackData(request, response);
            user.setUserId(userDataInRequest.getUserId());
            user.setNickname(userDataInRequest.getNickname());
            chatManager.onLoginSuccess(user);

            if (callback != null) {
                callback.onSuccess(user);
            }
        } else {
            ResponseError error = gson.fromJson(responseData.getResult(), ResponseError.class);
            callback.onError(error.getCode(), error.getMessage());
        }
    }
}
