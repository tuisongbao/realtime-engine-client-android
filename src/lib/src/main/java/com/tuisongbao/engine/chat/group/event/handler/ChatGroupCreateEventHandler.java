package com.tuisongbao.engine.chat.group.event.handler;

import com.google.gson.Gson;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.engine.chat.group.entity.ChatGroup;
import com.tuisongbao.engine.chat.group.entity.ChatGroupCreateData;
import com.tuisongbao.engine.common.entity.Event;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

public class ChatGroupCreateEventHandler extends BaseEventHandler<ChatGroup> {

    @Override
    protected ChatGroup prepareCallbackData(Event request, ResponseEventData response) {
        ChatGroup group = parse(response);

        if (!mEngine.chatManager.isCacheEnabled()) {
            return group;
        }

        String currentUser = mEngine.chatManager.getChatUser().getUserId();
        Gson gson = new Gson();
        ChatGroupCreateData requestData = gson.fromJson(request.getData(), ChatGroupCreateData.class);
        group.setOwner(currentUser);

        int userCount = 0;
        if (requestData.getInviteUserIds() != null) {
            userCount = requestData.getInviteUserIds().size();
        }
        // Add the current user
        userCount = userCount + 1;

        group.setUserCount(userCount);
        group.setIsPublic(requestData.isPublic());

        TSBGroupDataSource dataSource = new TSBGroupDataSource(TSBEngine.getContext(), mEngine.chatManager);
        dataSource.open();
        dataSource.insert(group, currentUser);
        dataSource.close();

        return group;
    }

    @Override
    public ChatGroup parse(ResponseEventData response) {
        ChatGroup group = new Gson().fromJson(response.getResult(),
                ChatGroup.class);
        return group;
    }
}
