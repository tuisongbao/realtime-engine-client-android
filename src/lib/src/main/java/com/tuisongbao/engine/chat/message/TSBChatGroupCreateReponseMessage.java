package com.tuisongbao.engine.chat.message;

import com.google.gson.Gson;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.TSBChatManager;
import com.tuisongbao.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.engine.chat.entity.TSBChatGroupCreateData;
import com.tuisongbao.engine.common.BaseTSBResponseMessage;

public class TSBChatGroupCreateReponseMessage extends
        BaseTSBResponseMessage<TSBChatGroup> {

    @Override
    protected TSBChatGroup prepareCallBackData() {
        TSBChatGroup group = super.prepareCallBackData();

        if (!TSBChatManager.getInstance().isCacheEnabled()) {
            return group;
        }

        String currentUser = TSBChatManager.getInstance().getChatUser().getUserId();
        Gson gson = new Gson();
        TSBChatGroupCreateData requestData = gson.fromJson((String)getRequestData(), TSBChatGroupCreateData.class);
        group.setOwner(currentUser);

        int userCount = 0;
        if (requestData.getInviteUserIds() != null) {
            userCount = requestData.getInviteUserIds().size();
        }
        // Add the current user
        userCount = userCount + 1;

        group.setUserCount(userCount);
        group.setIsPublic(requestData.isPublic());

        TSBGroupDataSource dataSource = new TSBGroupDataSource(TSBEngine.getContext());
        dataSource.open();
        dataSource.insert(group, currentUser);
        dataSource.close();

        return group;
    }

    @Override
    public TSBChatGroup parse() {
        TSBChatGroup group = new Gson().fromJson(getData(),
                TSBChatGroup.class);
        return group;
    }

}
