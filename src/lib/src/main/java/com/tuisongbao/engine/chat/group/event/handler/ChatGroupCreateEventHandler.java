package com.tuisongbao.engine.chat.group.event.handler;

import com.google.gson.Gson;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.db.ChatGroupDataSource;
import com.tuisongbao.engine.chat.group.entity.ChatGroup;
import com.tuisongbao.engine.chat.group.entity.ChatGroupEventData;
import com.tuisongbao.engine.chat.group.event.ChatGroupCreateEvent;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

public class ChatGroupCreateEventHandler extends BaseEventHandler<ChatGroup> {

    @Override
    protected ChatGroup genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        ChatGroup group = genCallbackData(request, response);

        String currentUser = engine.getChatManager().getChatUser().getUserId();
        ChatGroupEventData requestData = ((ChatGroupCreateEvent)request).getData();
        group.setOwner(currentUser);

        int userCount = 0;
        if (requestData.getInviteUserIds() != null) {
            userCount = requestData.getInviteUserIds().size();
        }
        // Add the current user
        userCount = userCount + 1;

        group.setUserCount(userCount);
        group.setIsPublic(requestData.isPublic());

        ChatGroupDataSource dataSource = new ChatGroupDataSource(Engine.getContext(), engine);
        dataSource.open();
        dataSource.insert(group, currentUser);
        dataSource.close();

        return group;
    }

    @Override
    public ChatGroup genCallbackData(BaseEvent request, RawEvent response) {
        ResponseEventData data = new Gson().fromJson(response.getData(), ResponseEventData.class);
        ChatGroup group = new Gson().fromJson(data.getResult(),
                ChatGroup.class);
        return group;
    }
}
