package com.tuisongbao.engine.chat.group;

import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.db.ChatGroupDataSource;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.BaseEventHandler;

import java.util.List;

class ChatGroupJoinInvitationEventHandler extends BaseEventHandler<String> {
    @Override
    protected String genCallbackData(BaseEvent request, RawEvent response) {
        ChatGroupDataSource groupDataSource = new ChatGroupDataSource(Engine.getContext(), engine);
        groupDataSource.open();

        ChatGroupEventData joinInvitationData = ((ChatGroupJoinInvitationEvent)request).getData();
        String groupId = joinInvitationData.getGroupId();
        List<String> userIds = joinInvitationData.getUserIds();

        for (String userId : userIds) {
            groupDataSource.insertUserIfNotExist(groupId, userId);
        }
        groupDataSource.close();
        return "OK";
    }

    @Override
    protected String genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        return "OK";
    }
}
