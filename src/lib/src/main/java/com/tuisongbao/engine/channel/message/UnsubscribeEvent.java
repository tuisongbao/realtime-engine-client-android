package com.tuisongbao.engine.channel.message;

import com.tuisongbao.engine.channel.entity.TSBChannel;
import com.tuisongbao.engine.common.event.BaseRequestEvent;

public class UnsubscribeEvent extends
        BaseRequestEvent<TSBChannel> {

    public static final String NAME = "engine_channel:unsubscribe";

    public UnsubscribeEvent() {
        super(NAME);
    }

}
