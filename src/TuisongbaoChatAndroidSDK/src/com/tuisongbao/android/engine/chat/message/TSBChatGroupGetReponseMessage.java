package com.tuisongbao.android.engine.chat.message;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupGetData;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;

public class TSBChatGroupGetReponseMessage extends
        BaseTSBResponseMessage<List<TSBChatGroup>> {

    @Override
    protected List<TSBChatGroup> prepareCallBackData() {
        List<TSBChatGroup> groups = super.prepareCallBackData();

        if (!TSBChatManager.getInstance().isCacheEnabled()) {
            return groups;
        }

        TSBGroupDataSource dataSource = new TSBGroupDataSource(TSBEngine.getContext());
        String userId = TSBChatManager.getInstance().getChatUser().getUserId();
        dataSource.open();
        dataSource.upsert(groups, userId);

        Gson gson = new Gson();
        gson.fromJson((String) getRequestData(), TSBChatGroupGetData.class);
        TSBChatGroupGetData requestData = gson.fromJson((String) getRequestData(), TSBChatGroupGetData.class);
        groups = dataSource.getList(requestData.getGroupId(), requestData.getName());
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
