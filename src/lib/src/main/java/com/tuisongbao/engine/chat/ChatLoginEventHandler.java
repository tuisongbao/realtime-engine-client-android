package com.tuisongbao.engine.chat;

import com.google.gson.Gson;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.BaseEventHandler;

class ChatLoginEventHandler extends BaseEventHandler<ChatUser> {

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
        ChatManager chatManager = engine.getChatManager();

        if (responseData.getOk()) {
            ChatLoginData data = ((ChatLoginEvent)request).getData();
            ChatUser userDataInRequest = new Gson().fromJson(data.getUserData(), ChatUser.class);

            ChatUser user = genCallbackData(request, response);
            user.setUserId(userDataInRequest.getUserId());
            user.setNickname(userDataInRequest.getNickname());

            chatManager.onLoginSuccess(user);
        } else {
            ResponseError error = gson.fromJson(responseData.getResult(), ResponseError.class);
            chatManager.onLoginFailed(error);
        }
    }
}
