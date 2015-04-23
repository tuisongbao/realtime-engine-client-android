package com.tuisongbao.android.engine.chat.message;

import com.google.gson.Gson;
import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupCreateData;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;

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
        group.setName(requestData.getName());
        group.setDescription(requestData.getDescription());

        TSBGroupDataSource dataSource = new TSBGroupDataSource(TSBEngine.getContext());
        dataSource.open();
        dataSource.insert(group);
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
