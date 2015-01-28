package com.tuisongbao.android.engine.connection.message;

import com.tuisongbao.android.engine.common.BaseTSBBindResponseMessage;
import com.tuisongbao.android.engine.connection.entity.TSBConnection;

public class TSBConnectionResponseMessage extends BaseTSBBindResponseMessage<TSBConnection> {

    @Override
    public TSBConnection parse() {
        TSBConnection connection = new TSBConnection();
        connection.setCode(getCode());
        connection.setMessage(getErrorMessage());
        return connection;
    }

}
