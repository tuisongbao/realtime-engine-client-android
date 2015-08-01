package com.tuisongbao.engine.chat.group.event;

import com.google.gson.Gson;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.engine.chat.group.entity.ChatGroup;
import com.tuisongbao.engine.chat.group.entity.ChatGroupCreateData;
import com.tuisongbao.engine.common.event.BaseResponseEvent;

public class ChatGroupCreateReponseEvent extends
        BaseResponseEvent<ChatGroup> {

    @Override
    protected ChatGroup prepareCallBackData() {
        ChatGroup group = super.prepareCallBackData();

        if (!mEngine.chatManager.isCacheEnabled()) {
            return group;
        }

        String currentUser = mEngine.chatManager.getChatUser().getUserId();
        Gson gson = new Gson();
        ChatGroupCreateData requestData = gson.fromJson((String)getRequestData(), ChatGroupCreateData.class);
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
    public ChatGroup parse() {
        ChatGroup group = new Gson().fromJson(getData(),
                ChatGroup.class);
        return group;
    }

}
