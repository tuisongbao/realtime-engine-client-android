package com.tuisongbao.engine.chat.group.event.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.engine.chat.group.entity.ChatGroup;
import com.tuisongbao.engine.chat.group.entity.ChatGroupGetData;
import com.tuisongbao.engine.chat.group.event.ChatGroupGetEvent;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

import java.util.List;

public class ChatGroupGetEventHandler extends BaseEventHandler<List<ChatGroup>> {

    @Override
    protected List<ChatGroup> genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        List<ChatGroup> groups = genCallbackData(request, response);

        TSBGroupDataSource dataSource = new TSBGroupDataSource(TSBEngine.getContext(), mEngine);
        String userId = mEngine.chatManager.getChatUser().getUserId();
        dataSource.open();
        dataSource.upsert(groups, userId);

        ChatGroupGetData requestData = ((ChatGroupGetEvent)request).getData();
        groups = dataSource.getList(userId, requestData.getGroupId());
        dataSource.close();

        return groups;
    }

    @Override
    public List<ChatGroup> genCallbackData(BaseEvent request, RawEvent response) {
        Gson gson = new Gson();
        ResponseEventData data = gson.fromJson(response.getData(), ResponseEventData.class);
        List<ChatGroup> list = new Gson().fromJson(data.getResult(),
                new TypeToken<List<ChatGroup>>() {
                }.getType());

        return list;
    }
}