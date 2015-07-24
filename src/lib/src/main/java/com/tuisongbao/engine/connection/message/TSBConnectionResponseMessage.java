package com.tuisongbao.engine.connection.message;

import com.tuisongbao.engine.common.BaseTSBResponseMessage;
import com.tuisongbao.engine.connection.entity.TSBConnection;

public class TSBConnectionResponseMessage extends BaseTSBResponseMessage<TSBConnection> {

    @Override
    public TSBConnection parse() {
        TSBConnection connection = new TSBConnection();
        connection.setCode(getCode());
        connection.setMessage(getErrorMessage());
        return connection;
    }

}
