package com.tuisongbao.engine.channel.message;

import com.tuisongbao.engine.channel.PresenceChannel;
import com.tuisongbao.engine.common.event.BaseEvent;

public class SubscribeEvent extends BaseEvent<PresenceChannel> {
    public static final String NAME = "engine_channel:subscribe";

    public SubscribeEvent() {
        super(NAME);
    }
}
