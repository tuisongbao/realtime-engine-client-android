package com.tuisongbao.android.engine.channel.entity;

public class TSBChannelSubscribeData {
    private String channel;
    private String signature;
    private TSBChannelUserData channelData;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public TSBChannelUserData getChannelData() {
        return channelData;
    }

    public void setChannelData(TSBChannelUserData channelData) {
        this.channelData = channelData;
    }

}
