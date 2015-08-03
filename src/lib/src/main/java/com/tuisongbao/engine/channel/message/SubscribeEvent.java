package com.tuisongbao.engine.channel.message;

import com.tuisongbao.engine.channel.entity.ChannelState;
import com.tuisongbao.engine.channel.entity.TSBPresenceChannel;
import com.tuisongbao.engine.common.event.BaseEvent;

public class SubscribeEvent extends
        BaseEvent<TSBPresenceChannel> {

    private transient String mAuthData;
    public static final String NAME = "engine_channel:subscribe";
    private transient ChannelState mState = ChannelState.INITIAL;

    public String getAuthData() {
        return mAuthData;
    }

    public void setAuthData(String authData) {
        this.mAuthData = authData;
    }

    public ChannelState getState() {
        return mState;
    }

    public boolean needSubscribeSend() {
        return mState == ChannelState.INITIAL || mState == ChannelState.SUBSCRIBE_SENT || mState == ChannelState.FAILED;
    }

    public boolean isSubscribeSending() {
        return mState == ChannelState.SUBSCRIBE_SENDING;
    }

    public boolean isSubscribed() {
        return mState == ChannelState.SUBSCRIBED;
    }

    public boolean isUnsubscribeed() {
        return mState == ChannelState.UNSUBSCRIBED;
    }

    public void setState(ChannelState state) {
        this.mState = state;
    }

    public SubscribeEvent() {
        super(NAME);
    }

}
