package com.tuisongbao.engine.channel.entity;

/**
 * Used to identify the state of the channel e.g. subscribed or unsubscribed.
 */
public enum ChannelState {
    INITIAL,
    SUBSCRIBE_SENDING,
    SUBSCRIBE_SENT,
    SUBSCRIBED,
    UNSUBSCRIBED,
    FAILED
}
