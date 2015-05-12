package com.tuisongbao.android.engine.chat.message;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupGetUsersData;
import com.tuisongbao.android.engine.chat.entity.TSBContactsUser;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;

public class TSBChatGroupGetUsersReponseMessage extends
        BaseTSBResponseMessage<List<TSBContactsUser>> {

    @Override
    protected List<TSBContactsUser> prepareCallBackData() {
        List<TSBContactsUser> users = super.prepareCallBackData();

        if (!TSBChatManager.getInstance().isCacheEnabled()) {
            return users;
        }

        TSBGroupDataSource dataSource = new TSBGroupDataSource(TSBEngine.getContext());
        dataSource.open();

        Gson gson = new Gson();
        TSBChatGroupGetUsersData requestData = gson.fromJson((String) getRequestData(), TSBChatGroupGetUsersData.class);
        String groupId = requestData.getGroupId();
        for (TSBContactsUser user : users) {
            dataSource.insertUserIfNotExist(groupId, user.getUserId());
        }
        dataSource.close();

        return users;
    }

    @Override
    public List<TSBContactsUser> parse() {
        List<TSBContactsUser> list = new Gson().fromJson(getData(),
                new TypeToken<List<TSBContactsUser>>() {
                }.getType());
        return list;
    }

}
