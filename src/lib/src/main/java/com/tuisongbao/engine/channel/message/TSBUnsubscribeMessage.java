package com.tuisongbao.engine.channel.message;

import com.tuisongbao.engine.channel.entity.TSBChannel;
import com.tuisongbao.engine.common.BaseTSBRequestMessage;

public class TSBUnsubscribeMessage extends
        BaseTSBRequestMessage<TSBChannel> {

    public static final String NAME = "engine_channel:unsubscribe";

    public TSBUnsubscribeMessage() {
        super(NAME);
    }

}
