package com.tuisongbao.engine.chat.group.event.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.db.ChatGroupDataSource;
import com.tuisongbao.engine.chat.group.entity.ChatGroupEventData;
import com.tuisongbao.engine.chat.group.event.ChatGroupGetUsersEvent;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

import java.util.List;

public class ChatGroupGetUsersEventHandler extends BaseEventHandler<List<ChatUser>> {

    @Override
    protected List<ChatUser> genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        List<ChatUser> users = genCallbackData(request, response);
        ChatGroupDataSource dataSource = new ChatGroupDataSource(Engine.getContext(), engine);
        dataSource.open();

        ChatGroupEventData requestData = ((ChatGroupGetUsersEvent)request).getData();
        String groupId = requestData.getGroupId();
        for (ChatUser user : users) {
            dataSource.insertUserIfNotExist(groupId, user.getUserId());
        }
        dataSource.close();

        return users;
    }

    @Override
    public List<ChatUser> genCallbackData(BaseEvent request, RawEvent response) {
        ResponseEventData data = new Gson().fromJson(response.getData(), ResponseEventData.class);
        List<ChatUser> list = new Gson().fromJson(data.getResult(),
                new TypeToken<List<ChatUser>>() {
                }.getType());
        return list;
    }

}
