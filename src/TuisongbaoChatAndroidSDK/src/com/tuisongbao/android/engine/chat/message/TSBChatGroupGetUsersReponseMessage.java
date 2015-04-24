package com.tuisongbao.android.engine.chat.message;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupGetUsersData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupUser;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;

public class TSBChatGroupGetUsersReponseMessage extends
        BaseTSBResponseMessage<List<TSBChatGroupUser>> {

    @Override
    protected List<TSBChatGroupUser> prepareCallBackData() {
        List<TSBChatGroupUser> users = super.prepareCallBackData();

        if (!TSBChatManager.getInstance().isCacheEnabled()) {
            return users;
        }

        TSBGroupDataSource dataSource = new TSBGroupDataSource(TSBEngine.getContext());
        dataSource.open();

        Gson gson = new Gson();
        TSBChatGroupGetUsersData requestData = gson.fromJson((String) getRequestData(), TSBChatGroupGetUsersData.class);
        String groupId = requestData.getGroupId();
        for (TSBChatGroupUser user : users) {
            dataSource.insertUserIfNotExist(groupId, user.getUserId());
        }
        dataSource.close();

        return users;
    }

    @Override
    public List<TSBChatGroupUser> parse() {
        List<TSBChatGroupUser> list = new Gson().fromJson(getData(),
                new TypeToken<List<TSBChatGroupUser>>() {
                }.getType());
        return list;
    }

}
