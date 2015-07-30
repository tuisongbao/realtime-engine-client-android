package com.tuisongbao.engine.connection.message;

import com.tuisongbao.engine.common.BaseTSBResponseMessage;
import com.tuisongbao.engine.connection.entity.TSBConnectionEvent;

public class TSBConnectionResponseMessage extends BaseTSBResponseMessage<TSBConnectionEvent> {

    @Override
    public TSBConnectionEvent parse() {
        TSBConnectionEvent connection = new TSBConnectionEvent();
        connection.setCode(getCode());
        connection.setMessage(getErrorMessage());
        return connection;
    }

}
