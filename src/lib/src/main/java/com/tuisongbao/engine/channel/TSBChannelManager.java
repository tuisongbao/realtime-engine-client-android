package com.tuisongbao.engine.channel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.channel.entity.TSBChannel;
import com.tuisongbao.engine.channel.entity.TSBPresenceChannel;
import com.tuisongbao.engine.channel.entity.TSBPrivateChannel;
import com.tuisongbao.engine.common.BaseManager;
import com.tuisongbao.engine.common.TSBEngineBindCallback;
import com.tuisongbao.engine.connection.entity.TSBConnectionEvent;
import com.tuisongbao.engine.entity.TSBEngineConstants;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.StrUtil;

public class TSBChannelManager extends BaseManager {
    private Map<String, TSBChannel> mChannelMap = new HashMap<String, TSBChannel>();
    private TSBEngineBindCallback mSubscribeErrorCallback = new TSBEngineBindCallback() {

        @Override
        public void onEvent(String channelName, Object... args) {
            LogUtil.info(LogUtil.LOG_TAG_CHANNEL, "Channel manager get subscribe error of " + channelName + ", remove it now");
            mChannelMap.remove(channelName);
        }
    };

    public TSBChannelManager(TSBEngine engine) {
        super(engine);
    }

    /**
     * 用于订阅channel，如需要订阅Private Channel，名字必须使用 private- 前缀
     * {@link TSBEngineConstants#TSBENGINE_CHANNEL_PREFIX_PRIVATE}，如需要订阅Presence
     * Channel，名字必须使用 presence- 前缀
     * {@link TSBEngineConstants#TSBENGINE_CHANNEL_PREFIX_PRESENCE}。
     *
     * @param authData
     */
    public TSBChannel subscribe(String channelName, String authData) {
        try {
            // unique instance return if the channel's name is same.
            TSBChannel channel = mChannelMap.get(channelName);
            if (channel != null) {
                return channel;
            }
            if (StrUtil.isEmpty(channelName)) {
                return null;
            }

            if (isPrivateChannel(channelName)) {
                channel = new TSBPrivateChannel(channelName, this);
            } else if (isPresenceChannel(channelName)) {
                TSBPresenceChannel presenceChannel = new TSBPresenceChannel(channelName, this);
                presenceChannel.setAuthData(authData);
                channel = presenceChannel;
            } else {
                // If not specified prefix found, default is public channel.
                channel = new TSBChannel(channelName, this);
            }
            LogUtil.info(LogUtil.LOG_TAG_CHANNEL, "Subscribe channel: " + channelName);
            mChannelMap.put(channelName, channel);
            channel.setEventListener();
            channel.subscribe();
            channel.bind("engine:subscription_error", mSubscribeErrorCallback);
            return channel;

        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, "Channel subscribe failed.", e);
            return null;
        }
    }

    public void unsubscribe(String channelName) {
        try {
            if (StrUtil.isEmpty(channelName)) {
                return;
            }
            TSBChannel channel = mChannelMap.get(channelName);
            if (channel != null) {
                channel.unsubscribe();
                mChannelMap.remove(channelName);
            }
        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, "Channel subscribe failed.", e);
        }
    }

    @Override
    protected void handleConnect(TSBConnectionEvent t) {
        LogUtil.info(LogUtil.LOG_TAG_CHANNEL, "Engine connected, resend all subscribe channel request");
        HashSet<TSBChannel> values = new HashSet<TSBChannel>(mChannelMap.values());
        for (TSBChannel channel : values) {
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
