package com.tuisongbao.engine.channel;

import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.common.EventEmitter;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.utils.LogUtils;

/**
 * <STRONG>普通 Channel</STRONG>
 *
 * <P>
 *     使用 {@link #bind(String, Listener)} 方法可以获取以下事件的回调通知：
 *
 * <UL>
 *     <LI>{@link #EVENT_SUBSCRIPTION_SUCCESS}</LI>
 *     <LI>{@link #EVENT_SUBSCRIPTION_ERROR}</LI>
 * </UL>
 */
public class Channel extends EventEmitter {
    /**
     * 订阅 Channel 成功时会触发该事件，对于普通 Channel，事件回调没有参数；
     * 对于 {@link PresenceChannel}，事件回调接收一个参数，类型为 {@code List<OnlineUser>}：
     *
     * <pre>
     *    connection.bind(Channel.EVENT_SUBSCRIPTION_SUCCESS, new Emitter.Listener() {
     *        &#64;Override
     *        public void call(final Object... args) {
     *            List&lt;OnlineUser&gt; onlineUsers = (List)args[0];
     *            Log.i(TAG, "当前在线用户有 " + onlineUsers.size() + " 个");
     *        }
     *    });
     * </pre>
     */
    public static final String EVENT_SUBSCRIPTION_SUCCESS = "engine:subscription_succeeded";
    /**
     * 订阅 Channel 失败时会触发该事件，事件回调接收一个参数，类型为 {@code String}，表明失败原因
     */
    public static final String EVENT_SUBSCRIPTION_ERROR = "engine:subscription_error";

    private static final String TAG = "TSB" + Channel.class.getSimpleName();

    final Engine engine;

    /**
     * This field must be channel, because when serialize message, this will be getCallbackData into it's name string.
     */
    String channel;

    public Channel(String name, Engine engine) {
        this.engine = engine;
        this.channel = name;
    }

    public String getName() {
        return channel;
    }

    void setName(String name) {
        channel = name;
    }

    /***
     * 订阅此 Channel, 针对不同的 Channel 会进行相应的鉴权
     * 该操作是异步的，需要通过绑定 engine:subscription_succeeded 和 engine:subscription_error Event 来获取订阅结果
     */
    public void subscribe() {
        LogUtils.debug(TAG, "Begin auth channel: " + channel);
        validate(new EngineCallback<String>() {

            @Override
            public void onSuccess(String t) {
                LogUtils.info(TAG, "Channel validation pass: " + t);
                try {
                    sendSubscribeRequest();
                } catch (Exception e) {
                    LogUtils.error(TAG, "Send subscribe request failed", e);
                }
            }

            @Override
            public void onError(ResponseError error) {
                LogUtils.info(TAG, "Channel validation failed: " + error.getMessage());
                trigger(EVENT_SUBSCRIPTION_ERROR, error.getMessage());

                // remove reference from ChannelManager
                engine.getChannelManager().unsubscribe(channel);
            }
        });
    }

    /**
     * 取消订阅此 Channel
     */
    public void unsubscribe() {
        try {
            UnsubscribeEvent message = new UnsubscribeEvent();
            Channel data = new Channel(channel, engine);
            message.setData(data);
            engine.getChannelManager().send(message, null);

            // Remove listeners on engineIO layer
            engine.getChannelManager().unbind(channel);
        } catch (Exception e) {
            LogUtils.error(TAG, e);
        }
    }

    private void sendSubscribeRequest() {
        SubscribeEvent message = generateSubscribeMessage();
        engine.getChannelManager().send(message, null);
    }

    void validate(EngineCallback<String> callback) {
        callback.onSuccess(channel);
    }

    SubscribeEvent generateSubscribeMessage() {
        SubscribeEvent message = new SubscribeEvent();
        // As PresenceChannel has all properties, so use it to be the event data.
        PresenceChannel data = new PresenceChannel(channel, engine);
        data.setName(channel);
        message.setData(data);

        return message;
    }

    @Override
    public String toString() {
        return "channel name:" + this.channel;
    }
}
