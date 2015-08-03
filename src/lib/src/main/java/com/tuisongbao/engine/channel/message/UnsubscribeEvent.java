package com.tuisongbao.engine.channel.message;

import com.tuisongbao.engine.channel.entity.TSBChannel;
import com.tuisongbao.engine.common.event.BaseEvent;

public class UnsubscribeEvent extends
        BaseEvent<TSBChannel> {

    public static final String NAME = "engine_channel:unsubscribe";

    public UnsubscribeEvent() {
        super(NAME);
    }

}
