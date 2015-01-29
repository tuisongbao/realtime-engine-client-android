package com.tuisongbao.android.engine.channel.message;

import com.tuisongbao.android.engine.channel.entity.TSBChannel;
import com.tuisongbao.android.engine.common.BaseTSBRequestMessage;

public class TSBUnsubscribeMessage extends
        BaseTSBRequestMessage<TSBChannel> {

    public static final String NAME = "engine_channel:unsubscribe";

    public TSBUnsubscribeMessage() {
        super(NAME);
    }

}
