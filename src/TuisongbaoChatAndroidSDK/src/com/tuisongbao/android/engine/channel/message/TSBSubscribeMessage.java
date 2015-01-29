package com.tuisongbao.android.engine.channel.message;

import com.tuisongbao.android.engine.channel.entity.ChannelState;
import com.tuisongbao.android.engine.channel.entity.TSBPresenceChannel;
import com.tuisongbao.android.engine.common.BaseTSBRequestMessage;

public class TSBSubscribeMessage extends
        BaseTSBRequestMessage<TSBPresenceChannel> {

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

    public TSBSubscribeMessage() {
        super(NAME);
    }

}
