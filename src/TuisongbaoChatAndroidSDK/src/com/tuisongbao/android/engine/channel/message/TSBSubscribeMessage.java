package com.tuisongbao.android.engine.channel.message;

import com.tuisongbao.android.engine.channel.entity.ChannelState;
import com.tuisongbao.android.engine.channel.entity.TSBChannelSubscribeData;
import com.tuisongbao.android.engine.common.BaseTSBRequestMessage;

public class TSBSubscribeMessage extends
        BaseTSBRequestMessage<TSBChannelSubscribeData> {

    public static final String NAME = "engine_channel:subscribe";
    private ChannelState mState = ChannelState.INITIAL;

    public ChannelState getState() {
        return mState;
    }

    public boolean needSend() {
        return mState == ChannelState.INITIAL || mState == ChannelState.SUBSCRIBE_SENT || mState == ChannelState.FAILED;
    }

    public boolean isSend() {
        return mState == ChannelState.SUBSCRIBE_SENDING;
    }

    public void setState(ChannelState state) {
        this.mState = state;
    }

    public TSBSubscribeMessage() {
        super(NAME);
    }

}
