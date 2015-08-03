package com.tuisongbao.engine.channel.entity;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.channel.message.SubscribeEvent;
import com.tuisongbao.engine.channel.message.UnsubscribeEvent;
import com.tuisongbao.engine.common.EventEmitter;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.callback.TSBEngineBindCallback;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.log.LogUtil;

import org.json.JSONException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class Channel extends EventEmitter {
    private static final String TAG = Channel.class.getSimpleName();

    protected TSBEngine engine;

    /**
     * This field must be channel, because when serialize message, this will be getCallbackData into it's name string.
     */
    protected String channel;
    transient ConcurrentMap<String, CopyOnWriteArrayList<TSBEngineBindCallback>> eventHandlers = new ConcurrentHashMap<String, CopyOnWriteArrayList<TSBEngineBindCallback>>();

    public Channel(String name, TSBEngine engine) {
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
        validate(new TSBEngineCallback<String>() {

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
            public void onError(int code, String message) {
                LogUtil.info(TAG, "Channel validation failed: " + message);
                handleErrorMessage(formatEventName(Protocol.CHANNEL_NAME_SUBSCRIPTION_ERROR), message);

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

            eventHandlers = new ConcurrentHashMap<>();
        } catch (Exception e) {

        }
    }

    protected void sendSubscribeRequest() throws JSONException {
        SubscribeEvent message = generateSubscribeMessage();
        engine.getChannelManager().send(message, null);
    }

    protected void validate(TSBEngineCallback<String> callback) {
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

    protected void handleErrorMessage(String eventName, String errorData) {
        List<TSBEngineBindCallback> errorCallbacks = eventHandlers.get(eventName);
        if (errorCallbacks == null) {
            return;
        }
        for (TSBEngineBindCallback callback : errorCallbacks) {
            callback.onEvent(getName(), eventName, errorData);
        }
    }

    /***
     *  Do not let developer know our internal event name.
     */
    protected String formatEventName(String origin) {
        return origin.replace("engine_channel", "engine");
    }
}
