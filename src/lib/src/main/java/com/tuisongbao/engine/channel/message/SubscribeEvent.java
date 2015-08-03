package com.tuisongbao.engine.channel.message;

import com.tuisongbao.engine.channel.entity.PresenceChannel;
import com.tuisongbao.engine.common.event.BaseEvent;

public class SubscribeEvent extends
        BaseEvent<PresenceChannel> {

    private transient String mAuthData;
    public static final String NAME = "engine_channel:subscribe";

    public void setAuthData(String authData) {
        this.mAuthData = authData;
    }

    public SubscribeEvent() {
        super(NAME);
    }

}
