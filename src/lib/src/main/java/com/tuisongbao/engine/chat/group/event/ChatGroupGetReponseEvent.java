package com.tuisongbao.engine.chat.group.event;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.engine.chat.group.entity.ChatGroup;
import com.tuisongbao.engine.chat.group.entity.ChatGroupGetData;
import com.tuisongbao.engine.common.event.BaseResponseEvent;

public class ChatGroupGetReponseEvent extends
        BaseResponseEvent<List<ChatGroup>> {

    @Override
    protected List<ChatGroup> prepareCallBackData() {
        List<ChatGroup> groups = super.prepareCallBackData();

        if (!mEngine.chatManager.isCacheEnabled()) {
            return groups;
        }

        TSBGroupDataSource dataSource = new TSBGroupDataSource(TSBEngine.getContext(), mEngine.chatManager);
        String userId = mEngine.chatManager.getChatUser().getUserId();
        dataSource.open();
        dataSource.upsert(groups, userId);

        Gson gson = new Gson();
        ChatGroupGetData requestData = gson.fromJson((String) getRequestData(), ChatGroupGetData.class);
        groups = dataSource.getList(userId, requestData.getGroupId());
        dataSource.close();

        return groups;
    }

    @Override
    public List<ChatGroup> parse() {
        List<ChatGroup> list = new Gson().fromJson(getData(),
                new TypeToken<List<ChatGroup>>() {
                }.getType());

        return list;
    }
}
