package com.tuisongbao.android.engine.chat.message;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;

public class TSBChatGroupGetReponseMessage extends
        BaseTSBResponseMessage<List<TSBChatGroup>> {

    @Override
    protected void preCallBack(List<TSBChatGroup> groups) {
        super.preCallBack(groups);

        if (!TSBChatManager.getInstance().isUseCache()) {
            return;
        }

        TSBGroupDataSource dataSource = new TSBGroupDataSource(TSBEngine.getContext());
        dataSource.open();
        for (TSBChatGroup group : groups) {
            dataSource.insert(group);
        }
        dataSource.close();
    }

    @Override
    public List<TSBChatGroup> parse() {
        List<TSBChatGroup> list = new Gson().fromJson(getData(),
                new TypeToken<List<TSBChatGroup>>() {
                }.getType());

        return list;
    }
}
