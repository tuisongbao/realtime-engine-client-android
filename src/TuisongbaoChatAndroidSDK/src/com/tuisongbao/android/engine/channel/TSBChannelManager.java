package com.tuisongbao.android.engine.channel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.tuisongbao.android.engine.channel.entity.TSBChannel;
import com.tuisongbao.android.engine.channel.entity.TSBPresenceChannel;
import com.tuisongbao.android.engine.channel.entity.TSBPrivateChannel;
import com.tuisongbao.android.engine.common.BaseManager;
import com.tuisongbao.android.engine.connection.entity.TSBConnection;
import com.tuisongbao.android.engine.entity.TSBEngineConstants;
import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBChannelManager extends BaseManager {

    Map<String, TSBChannel> mChannelMap = new HashMap<String, TSBChannel>();

    private static TSBChannelManager mInstance;

    public synchronized static TSBChannelManager getInstance() {
        if (mInstance == null) {
            mInstance = new TSBChannelManager();
        }
        return mInstance;
    }

    private TSBChannelManager() {
        super();
    }

    /**
     * 订阅公用channel并绑定渠道事件
     *
     * @param channel
     */
    public TSBChannel subscribePublicChannel(String channel) {
        return subscribe(channel, null);
    }

    /**
     * 订阅 private channel并绑定渠道事件, 名字必须使用 private- 前缀
     *
     * @param channel
     */
    public TSBChannel subscribePrivateChannel(String channel) {
        return subscribe(channel, null);
    }
    /**
     * 订阅 presence channel并绑定渠道事件, 名字必须使用 presence- 前缀
     *
     * @param channel
     * @param authData 用户信息
     */
    public TSBChannel subscribePresenceChannel(String channel, String authData) {
        return subscribe(channel, authData);
    }

    /**
     * 用于订阅channel，如需要订阅Private Channel，名字必须使用 private- 前缀
     * {@link TSBEngineConstants#TSBENGINE_CHANNEL_PREFIX_PRIVATE}，如需要订阅Presence
     * Channel，名字必须使用 presence- 前缀
     * {@link TSBEngineConstants#TSBENGINE_CHANNEL_PREFIX_PRESENCE}。
     *
     * @param channel
     * @param authData
     * @param callback
     */
    private TSBChannel subscribe(String channelName, String authData) {
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
                channel = new TSBPrivateChannel(channelName);
            } else if (isPresenceChannel(channelName)) {
                TSBPresenceChannel presenceChannel = new TSBPresenceChannel(channelName);
                presenceChannel.setAuthData(authData);
                channel = presenceChannel;
            } else {
                // If not specified prefix found, default is public channel.
                channel = new TSBChannel(channelName);
            }
            LogUtil.info(LogUtil.LOG_TAG_CHANNEL, "Subscribe channel: " + channelName);
            mChannelMap.put(channelName, channel);
            channel.subscribe();
            return channel;

        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, "Channel subscribe failed.", e);
            return null;
        }
    }

    /**
     * 取消订阅
     *
     * @param channel
     * @param authData
     * @param callback
     */
    public void unSubscribe(String channelName) {
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
    protected void handleConnect(TSBConnection t) {
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
