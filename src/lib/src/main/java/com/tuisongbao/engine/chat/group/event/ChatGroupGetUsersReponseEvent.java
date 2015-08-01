package com.tuisongbao.engine.chat.group.event;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.engine.chat.group.entity.ChatGroupGetUsersData;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.common.event.BaseResponseEvent;

import java.util.List;

public class ChatGroupGetUsersReponseEvent extends
        BaseResponseEvent<List<ChatUser>> {

    @Override
    protected List<ChatUser> prepareCallBackData() {
        List<ChatUser> users = super.prepareCallBackData();

        if (!mEngine.chatManager.isCacheEnabled()) {
            return users;
        }

        TSBGroupDataSource dataSource = new TSBGroupDataSource(TSBEngine.getContext(), mEngine.chatManager);
        dataSource.open();

        Gson gson = new Gson();
        ChatGroupGetUsersData requestData = gson.fromJson((String) getRequestData(), ChatGroupGetUsersData.class);
        String groupId = requestData.getGroupId();
        for (ChatUser user : users) {
            dataSource.insertUserIfNotExist(groupId, user.getUserId());
        }
        dataSource.close();

        return users;
    }

    @Override
    public List<ChatUser> parse() {
        List<ChatUser> list = new Gson().fromJson(getData(),
                new TypeToken<List<ChatUser>>() {
                }.getType());
        return list;
    }

}
