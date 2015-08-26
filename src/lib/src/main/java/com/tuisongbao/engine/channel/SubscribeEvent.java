package com.tuisongbao.engine.channel;

import com.tuisongbao.engine.common.event.BaseEvent;

class SubscribeEvent extends BaseEvent<PresenceChannel> {
    public static final String NAME = "engine_channel:subscribe";

    public SubscribeEvent() {
        super(NAME);
    }
}
