package com.tuisongbao.android.engine.chat.message;

import com.google.gson.Gson;
import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;

public class TSBChatGroupCreateReponseMessage extends
        BaseTSBResponseMessage<TSBChatGroup> {

    @Override
    protected void preCallBack(TSBChatGroup group) {
        super.preCallBack(group);

        if (!TSBChatManager.getInstance().isUseCache()) {
            return;
        }

        TSBGroupDataSource dataSource = new TSBGroupDataSource(TSBEngine.getContext());
        dataSource.open();
        dataSource.insert(group);
        dataSource.close();
    }

    @Override
    public TSBChatGroup parse() {
        TSBChatGroup group = new Gson().fromJson(getData(),
                TSBChatGroup.class);
        return group;
    }

}
