package com.tuisongbao.engine.channel;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.common.BaseManager;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.utils.LogUtils;
import com.tuisongbao.engine.utils.StrUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * <STRONG>Pub/Sub 管理类</STRONG>
 *
 * <P>
 *     推送宝 {@link Engine} 中，Pub/Sub 模块的管理类。
 *     可通过调用 {@link Engine#getChannelManager()} 获得该实例。
 *     网络掉线后，当网络再次可用时，会重新 {@link #subscribe(String, String)} 所有已经订阅成功的 {@code Channel}。
 */
public final class ChannelManager extends BaseManager {
    private static final String TAG = "TSB" + ChannelManager.class.getSimpleName();

    private final Map<String, Channel> mChannelMap = new HashMap<>();

    public ChannelManager(Engine engine) {
        super(engine);
        bind(Protocol.CHANNEL_EVENT_SUBSCRIPTION_ERROR, new Listener() {
            @Override
            public void call(Object... args) {
                RawEvent event = (RawEvent)args[0];
                String channelName = event.getChannel();
                Channel channel = mChannelMap.get(channelName);

                if (channel != null) {
                    String message = event.getData().getAsJsonObject().get("message").getAsString();
                    channel.trigger(Channel.EVENT_SUBSCRIPTION_ERROR, message);

                    mChannelMap.remove(channelName);
                }
            }
        });

        bind(Protocol.CHANNEL_EVENT_SUBSCRIPTION_SUCCESS, new Listener() {
            @Override
            public void call(Object... args) {
                RawEvent event = (RawEvent)args[0];
                String channelName = event.getChannel();
                Channel channel = mChannelMap.get(channelName);

                if (channel == null) {
                    return;
                }
                JsonElement data = event.getData();
                if (isPresenceChannel(channel.getName())) {
                    List<PresenceChannelOnlineUser> presenceChannelOnlineUsers = new Gson().fromJson(data,
                            new TypeToken<List<PresenceChannelOnlineUser>>(){}.getType());
                    channel.trigger(Channel.EVENT_SUBSCRIPTION_SUCCESS, presenceChannelOnlineUsers);
                } else {
                    channel.trigger(Channel.EVENT_SUBSCRIPTION_SUCCESS);
                }
            }
        });

        Listener presenceUserStatusListener = new Listener() {
            @Override
            public void call(Object... args) {
                RawEvent event = (RawEvent)args[0];
                PresenceChannelUser presenceChannelUser = new Gson().fromJson(event.getData(), PresenceChannelUser.class);
                String channelName = event.getChannel();
                Channel channel = mChannelMap.get(channelName);

                if (channel != null) {
                    channel.trigger(trimInternalSign(event.getName()), presenceChannelUser);
                }
            }
        };

        bind(Protocol.CHANNEL_EVENT_USER_ADDED, presenceUserStatusListener);
        bind(Protocol.CHANNEL_EVENT_USER_REMOVED, presenceUserStatusListener);
    }

    /**
     * 订阅 channel 并返回 channel 实例。通过给 channelName 添加不同的前缀来区分不同类型的 channel.
     * public channel 不需要前缀； private channel 以 `private-` 为前缀； presence channel 以 `presence-` 为前缀, 针对不同的 Channel 会进行相应的鉴权
     *
     * 该操作是异步的，需要通过绑定 engine:subscription_succeeded 和 engine:subscription_error Event 来获取订阅结果
     *
     * @param channelName   必填
     * @param authData      在创建 {@link PresenceChannel} 时必须指定该值，其它的可设置为 null
     * @return Channel 实例
     */
    public Channel subscribe(String channelName, String authData) {
        try {
            // unique instance return if the channel's name is same.
            Channel channel = mChannelMap.get(channelName);
            if (channel != null) {
                return channel;
            }
            if (StrUtils.isEmpty(channelName)) {
                return null;
            }

            if (isPrivateChannel(channelName)) {
                channel = new PrivateChannel(channelName, engine);
            } else if (isPresenceChannel(channelName)) {
                PresenceChannel presenceChannel = new PresenceChannel(channelName, engine);
                presenceChannel.setAuthData(authData);
                channel = presenceChannel;
            } else {
                // If not specified prefix found, default is public channel.
                channel = new Channel(channelName, engine);
            }
            LogUtils.info(TAG, "Subscribe channel: " + channelName);
            mChannelMap.put(channelName, channel);
            if (engine.getConnection().isConnected()) {
                channel.subscribe();
            }
            return channel;

        } catch (Exception e) {
            LogUtils.error(TAG, "Channel subscribe failed.", e);
            return null;
        }
    }

    /***
     * 取消订阅 Channel
     *
     * @param channelName 必填
     */
    public void unsubscribe(String channelName) {
        try {
            if (StrUtils.isEmpty(channelName)) {
                return;
            }
            Channel channel = mChannelMap.get(channelName);
            if (channel != null) {
                channel.unsubscribe();
                mChannelMap.remove(channelName);
            }
        } catch (Exception e) {
            LogUtils.error(TAG, "Channel subscribe failed.", e);
        }
    }

    public void channelEventReceived(String channelName, RawEvent event) {
        Channel channel = mChannelMap.get(channelName);
        if (channel != null) {
            channel.trigger(event.getName(), event.getData());
        }
    }

    @Override
    protected void connected() {
        LogUtils.info(TAG, "Resend all subscribe channel request");
        HashSet<Channel> values = new HashSet<>(mChannelMap.values());
        for (Channel channel : values) {
            channel.subscribe();
        }
    }

    @Override
    protected void disconnected() {
        // Unbind all listeners of channel, because when connected, it will subscribe again. In case multiple trigger.
        HashSet<Channel> values = new HashSet<>(mChannelMap.values());
        for (Channel channel : values) {
            unbind(channel.getName());
        }
    }

    private boolean isPrivateChannel(String channel) {
        return channel.startsWith("private-");
    }

    private boolean isPresenceChannel(String channel) {
        return channel.startsWith("presence-");
    }

    private String trimInternalSign(String eventName) {
        return StrUtils.invokeRegxReplace(eventName, "engine_channel", "engine");
    }
}
