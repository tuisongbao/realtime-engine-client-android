package com.tuisongbao.engine.channel;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.channel.entity.Channel;
import com.tuisongbao.engine.channel.entity.PrivateChannel;
import com.tuisongbao.engine.channel.entity.PresenceChannel;
import com.tuisongbao.engine.common.BaseManager;
import com.tuisongbao.engine.connection.entity.ConnectionEventData;
import com.tuisongbao.engine.common.TSBEngineConstants;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.StrUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ChannelManager extends BaseManager {
    private static final String TAG = ChannelManager.class.getSimpleName();

    private Map<String, Channel> mChannelMap = new HashMap<>();

    public ChannelManager(TSBEngine engine) {
        super(engine);
        bind("engine:subscription_error", new Listener() {
            @Override
            public void call(Object... args) {
                String channelName = args[0].toString();
                mChannelMap.remove(channelName);
            }
        });
    }

    /**
     * 用于订阅channel，如需要订阅Private Channel，名字必须使用 private- 前缀
     * {@link TSBEngineConstants#TSBENGINE_CHANNEL_PREFIX_PRIVATE}，如需要订阅Presence
     * Channel，名字必须使用 presence- 前缀
     * {@link TSBEngineConstants#TSBENGINE_CHANNEL_PREFIX_PRESENCE}。
     *
     * @param authData
     */
    public Channel subscribe(String channelName, String authData) {
        try {
            // unique instance return if the channel's name is same.
            Channel channel = mChannelMap.get(channelName);
            if (channel != null) {
                return channel;
            }
            if (StrUtil.isEmpty(channelName)) {
                return null;
            }

            if (isPrivateChannel(channelName)) {
                channel = new PrivateChannel(channelName, this);
            } else if (isPresenceChannel(channelName)) {
                PresenceChannel presenceChannel = new PresenceChannel(channelName, this);
                presenceChannel.setAuthData(authData);
                channel = presenceChannel;
            } else {
                // If not specified prefix found, default is public channel.
                channel = new Channel(channelName, this);
            }
            LogUtil.info(TAG, "Subscribe channel: " + channelName);
            mChannelMap.put(channelName, channel);
            channel.subscribe();
            return channel;

        } catch (Exception e) {
            LogUtil.error(TAG, "Channel subscribe failed.", e);
            return null;
        }
    }

    public void unsubscribe(String channelName) {
        try {
            if (StrUtil.isEmpty(channelName)) {
                return;
            }
            Channel channel = mChannelMap.get(channelName);
            if (channel != null) {
                channel.unsubscribe();
                mChannelMap.remove(channelName);
            }
        } catch (Exception e) {
            LogUtil.error(TAG, "Channel subscribe failed.", e);
        }
    }

    @Override
    protected void handleConnect(ConnectionEventData t) {
        LogUtil.info(TAG, "Engine connected, resend all subscribe channel request");
        HashSet<Channel> values = new HashSet<Channel>(mChannelMap.values());
        for (Channel channel : values) {
            channel.subscribe();
        }
    }

    private boolean isPrivateChannel(String channel) {
        return channel
                .startsWith(TSBEngineConstants.TSBENGINE_CHANNEL_PREFIX_PRIVATE);
    }

    private boolean isPresenceChannel(String channel) {
        return channel
                .startsWith(TSBEngineConstants.TSBENGINE_CHANNEL_PREFIX_PRESENCE);
    }
}
