package com.tuisongbao.engine.chat.group.event.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.db.ChatGroupDataSource;
import com.tuisongbao.engine.chat.group.entity.ChatGroupEventData;
import com.tuisongbao.engine.chat.group.entity.ChatGroupUser;
import com.tuisongbao.engine.chat.group.event.ChatGroupGetUsersEvent;
import com.tuisongbao.engine.chat.serializer.ChatUserPresenceSerializer;
import com.tuisongbao.engine.chat.user.entity.ChatUserPresence;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

import java.util.List;

public class ChatGroupGetUsersEventHandler extends BaseEventHandler<List<ChatGroupUser>> {

    @Override
    protected List<ChatGroupUser> genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        List<ChatGroupUser> users = genCallbackData(request, response);
        ChatGroupDataSource dataSource = new ChatGroupDataSource(Engine.getContext(), engine);
        dataSource.open();

        ChatGroupEventData requestData = ((ChatGroupGetUsersEvent)request).getData();
        String groupId = requestData.getGroupId();
        for (ChatGroupUser user : users) {
            dataSource.insertUserIfNotExist(groupId, user.getUserId());
        }
        dataSource.close();

        return users;
    }

    @Override
    public List<ChatGroupUser> genCallbackData(BaseEvent request, RawEvent response) {
        ResponseEventData data = new Gson().fromJson(response.getData(), ResponseEventData.class);
        Gson gson = new GsonBuilder().registerTypeAdapter(ChatUserPresence.Presence.class,
                new ChatUserPresenceSerializer()).create();
        List<ChatGroupUser> list = gson.fromJson(data.getResult(),
                new TypeToken<List<ChatGroupUser>>() {
                }.getType());
        return list;
    }

}
