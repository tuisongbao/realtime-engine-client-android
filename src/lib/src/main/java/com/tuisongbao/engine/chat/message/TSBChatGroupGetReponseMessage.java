package com.tuisongbao.engine.chat.message;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.TSBChatManager;
import com.tuisongbao.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.engine.chat.entity.TSBChatGroupGetData;
import com.tuisongbao.engine.common.BaseTSBResponseMessage;

public class TSBChatGroupGetReponseMessage extends
        BaseTSBResponseMessage<List<TSBChatGroup>> {

    @Override
    protected List<TSBChatGroup> prepareCallBackData() {
        List<TSBChatGroup> groups = super.prepareCallBackData();

        if (!mEngine.chatManager.isCacheEnabled()) {
            return groups;
        }

        TSBGroupDataSource dataSource = new TSBGroupDataSource(TSBEngine.getContext(), mEngine.chatManager);
        String userId = mEngine.chatManager.getChatUser().getUserId();
        dataSource.open();
        dataSource.upsert(groups, userId);

        Gson gson = new Gson();
        TSBChatGroupGetData requestData = gson.fromJson((String) getRequestData(), TSBChatGroupGetData.class);
        groups = dataSource.getList(userId, requestData.getGroupId());
        dataSource.close();

        return groups;
    }

    @Override
    public List<TSBChatGroup> parse() {
        List<TSBChatGroup> list = new Gson().fromJson(getData(),
                new TypeToken<List<TSBChatGroup>>() {
                }.getType());

        return list;
    }
}
