package com.tuisongbao.engine.chat.group.event.handler;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.engine.chat.group.entity.ChatGroupRemoveUserData;
import com.tuisongbao.engine.chat.group.event.ChatGroupRemoveUsersEvent;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

/**
 * Created by root on 15-8-3.
 */
public class ChatGroupRemoveUsersEventHandler extends BaseEventHandler<String> {
    @Override
    protected String genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        TSBGroupDataSource groupDataSource = new TSBGroupDataSource(TSBEngine.getContext(), mEngine);
        ChatGroupRemoveUserData removeUserData = ((ChatGroupRemoveUsersEvent)request).getData();

        groupDataSource.open();
        String groupId = removeUserData.getGroupId();
        for (String userId : removeUserData.getUserIds()) {
            groupDataSource.removeUser(groupId, userId);
        }
        groupDataSource.close();
        return "OK";
    }

    @Override
    protected String genCallbackData(BaseEvent request, RawEvent response) {
        return "OK";
    }
}