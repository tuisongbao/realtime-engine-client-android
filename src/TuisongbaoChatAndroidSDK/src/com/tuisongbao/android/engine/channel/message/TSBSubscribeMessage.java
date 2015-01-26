package com.tuisongbao.android.engine.channel.message;

import com.tuisongbao.android.engine.channel.entity.TSBChannelSubscribeData;
import com.tuisongbao.android.engine.common.BaseTSBRequestMessage;

public class TSBSubscribeMessage extends
        BaseTSBRequestMessage<TSBChannelSubscribeData> {

    public static final String NAME = "engine_channel:subscribe";

    public TSBSubscribeMessage() {
        super(NAME);
    }

}
