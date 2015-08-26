package com.tuisongbao.engine.chat.group;

import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.db.ChatGroupDataSource;
import com.tuisongbao.engine.chat.ChatUser;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.BaseEventHandler;

class ChatGroupLeaveEventHandler extends BaseEventHandler<String> {
    @Override
    protected String genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        ChatGroupEventData leaveData = ((ChatGroupLeaveEvent)request).getData();
        ChatGroupDataSource groupDataSource = new ChatGroupDataSource(Engine.getContext(), engine);
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
