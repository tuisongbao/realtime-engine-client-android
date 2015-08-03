package com.tuisongbao.engine.chat.group.event.handler;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.engine.chat.group.entity.ChatGroupJoinInvitationData;
import com.tuisongbao.engine.chat.group.event.ChatGroupJoinInvitationEvent;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

import java.util.List;

/**
 * Created by root on 15-8-3.
 */
public class ChatGroupJoinInvitationEventHandler extends BaseEventHandler<String> {
    @Override
    protected String genCallbackData(BaseEvent request, RawEvent response) {
        TSBGroupDataSource groupDataSource = new TSBGroupDataSource(TSBEngine.getContext(), mEngine);
        groupDataSource.open();

        ChatGroupJoinInvitationData joinInvitationData = ((ChatGroupJoinInvitationEvent)request).getData();
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