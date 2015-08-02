package com.tuisongbao.engine.chat.group.event.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.engine.chat.group.entity.ChatGroup;
import com.tuisongbao.engine.chat.group.entity.ChatGroupGetData;
import com.tuisongbao.engine.common.entity.Event;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

import java.util.List;

public class ChatGroupGetEventHandler extends BaseEventHandler<List<ChatGroup>> {

    @Override
    protected List<ChatGroup> prepareCallbackData(Event request, ResponseEventData response) {
        List<ChatGroup> groups = parse(response);

        if (!mEngine.chatManager.isCacheEnabled()) {
            return groups;
        }

        TSBGroupDataSource dataSource = new TSBGroupDataSource(TSBEngine.getContext(), mEngine);
        String userId = mEngine.chatManager.getChatUser().getUserId();
        dataSource.open();
        dataSource.upsert(groups, userId);

        Gson gson = new Gson();
        ChatGroupGetData requestData = gson.fromJson(request.getData(), ChatGroupGetData.class);
        groups = dataSource.getList(userId, requestData.getGroupId());
        dataSource.close();

        return groups;
    }

    @Override
    public List<ChatGroup> parse(ResponseEventData response) {
        List<ChatGroup> list = new Gson().fromJson(response.getResult(),
                new TypeToken<List<ChatGroup>>() {
                }.getType());

        return list;
    }
}
