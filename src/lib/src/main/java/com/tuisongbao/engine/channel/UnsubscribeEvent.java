package com.tuisongbao.engine.channel;

import com.tuisongbao.engine.common.event.BaseEvent;

class UnsubscribeEvent extends
        BaseEvent<Channel> {

    public static final String NAME = "engine_channel:unsubscribe";

    public UnsubscribeEvent() {
        super(NAME);
    }

}
