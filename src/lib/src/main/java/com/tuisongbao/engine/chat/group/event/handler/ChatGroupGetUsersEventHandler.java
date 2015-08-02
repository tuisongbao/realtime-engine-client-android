package com.tuisongbao.engine.chat.group.event.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.engine.chat.group.entity.ChatGroupGetUsersData;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.common.entity.Event;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

import java.util.List;

public class ChatGroupGetUsersEventHandler extends BaseEventHandler<List<ChatUser>> {

    @Override
    protected List<ChatUser> prepareCallbackData(Event request, ResponseEventData response) {
        List<ChatUser> users = parse(response);

        if (!mEngine.chatManager.isCacheEnabled()) {
            return users;
        }

        TSBGroupDataSource dataSource = new TSBGroupDataSource(TSBEngine.getContext(), mEngine.chatManager);
        dataSource.open();

        Gson gson = new Gson();
        ChatGroupGetUsersData requestData = gson.fromJson(request.getData(), ChatGroupGetUsersData.class);
        String groupId = requestData.getGroupId();
        for (ChatUser user : users) {
            dataSource.insertUserIfNotExist(groupId, user.getUserId());
        }
        dataSource.close();

        return users;
    }

    @Override
    public List<ChatUser> parse(ResponseEventData response) {
        List<ChatUser> list = new Gson().fromJson(response.getResult(),
                new TypeToken<List<ChatUser>>() {
                }.getType());
        return list;
    }

}
