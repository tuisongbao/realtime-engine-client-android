package com.tuisongbao.engine.chat.group.event.handler;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.db.ChatGroupDataSource;
import com.tuisongbao.engine.chat.group.entity.ChatGroupEventData;
import com.tuisongbao.engine.chat.group.event.ChatGroupLeaveEvent;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

/**
 * Created by root on 15-8-3.
 */
public class ChatGroupLeaveEventHandler extends BaseEventHandler<String> {
    @Override
    protected String genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        ChatGroupEventData leaveData = ((ChatGroupLeaveEvent)request).getData();
        ChatGroupDataSource groupDataSource = new ChatGroupDataSource(TSBEngine.getContext(), engine);
        ChatUser chatUser = engine.getChatManager().getChatUser();

        groupDataSource.open();
        groupDataSource.removeUser(leaveData.getGroupId(), chatUser.getUserId());
        groupDataSource.close();

        return "OK";
    }

    @Override
    protected String genCallbackData(BaseEvent request, RawEvent response) {
        return "OK";
    }
}
