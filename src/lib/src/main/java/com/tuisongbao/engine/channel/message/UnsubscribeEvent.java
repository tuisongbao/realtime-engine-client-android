package com.tuisongbao.engine.channel.message;

import com.tuisongbao.engine.channel.Channel;
import com.tuisongbao.engine.common.event.BaseEvent;

public class UnsubscribeEvent extends
        BaseEvent<Channel> {

    public static final String NAME = "engine_channel:unsubscribe";

    public UnsubscribeEvent() {
        super(NAME);
    }

}
