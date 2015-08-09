package com.tuisongbao.engine.channel;

import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.channel.message.SubscribeEvent;
import com.tuisongbao.engine.channel.message.UnsubscribeEvent;
import com.tuisongbao.engine.common.EventEmitter;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.log.LogUtil;

import org.json.JSONException;

public class Channel extends EventEmitter {
    private static final String TAG = "TSB" + Channel.class.getSimpleName();

    protected Engine engine;

    /**
     * This field must be channel, because when serialize message, this will be getCallbackData into it's name string.
     */
    protected String channel;

    public Channel(String name, Engine engine) {
        this.engine = engine;
        this.channel = name;
    }

    public String getName() {
        return channel;
    }

    public void setName(String name) {
        channel = name;
    }

    public void subscribe() {
        LogUtil.debug(TAG, "Begin auth channel: " + channel);
        validate(new EngineCallback<String>() {

            @Override
            public void onSuccess(String t) {
                LogUtil.info(TAG, "Channel validation pass: " + t);
                try {
                    sendSubscribeRequest();
                } catch (Exception e) {
                    LogUtil.error(TAG, "Send subscribe request failed", e);
                }
            }

            @Override
            public void onError(ResponseError error) {
                LogUtil.info(TAG, "Channel validation failed: " + error.getMessage());
                trigger(formatEventName(Protocol.CHANNEL_EVENT_SUBSCRIPTION_ERROR), error.getMessage());

                // remove reference from tsbchannel manager
                engine.getChannelManager().unsubscribe(channel);
            }
        });
    }

    public void unsubscribe() {
        try {
            UnsubscribeEvent message = new UnsubscribeEvent();
            Channel data = new Channel(channel, engine);
            message.setData(data);
            engine.getChannelManager().send(message, null);

            // Remove listeners on engineIO layer
            engine.getChannelManager().unbind(channel);
        } catch (Exception e) {

        }
    }

    protected void sendSubscribeRequest() throws JSONException {
        SubscribeEvent message = generateSubscribeMessage();
        engine.getChannelManager().send(message, null);
    }

    protected void validate(EngineCallback<String> callback) {
        callback.onSuccess(channel);
    }

    protected SubscribeEvent generateSubscribeMessage() {
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

    /***
     *  Do not let developer know our internal event name.
     */
    protected String formatEventName(String origin) {
        return origin.replace("engine_channel", "engine");
    }
}
