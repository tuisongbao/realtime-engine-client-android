package com.tuisongbao.engine.chat.group.event.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.db.ChatGroupDataSource;
import com.tuisongbao.engine.chat.group.entity.ChatGroupEventData;
import com.tuisongbao.engine.chat.group.event.ChatGroupGetUsersEvent;
import com.tuisongbao.engine.chat.user.entity.ChatUserPresenceData;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

import java.util.List;

public class ChatGroupGetUsersEventHandler extends BaseEventHandler<List<ChatUserPresenceData>> {

    @Override
    protected List<ChatUserPresenceData> genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        List<ChatUserPresenceData> users = genCallbackData(request, response);
        ChatGroupDataSource dataSource = new ChatGroupDataSource(Engine.getContext(), engine);
        dataSource.open();

        ChatGroupEventData requestData = ((ChatGroupGetUsersEvent)request).getData();
        String groupId = requestData.getGroupId();
        for (ChatUserPresenceData user : users) {
            dataSource.insertUserIfNotExist(groupId, user.getUserId());
        }
        dataSource.close();

        return users;
    }

    @Override
    public List<ChatUserPresenceData> genCallbackData(BaseEvent request, RawEvent response) {
        ResponseEventData data = new Gson().fromJson(response.getData(), ResponseEventData.class);
        List<ChatUserPresenceData> list = new Gson().fromJson(data.getResult(),
                new TypeToken<List<ChatUserPresenceData>>() {
                }.getType());
        return list;
    }

}
