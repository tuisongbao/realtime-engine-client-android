package com.tuisongbao.engine.channel.entity;

import com.tuisongbao.engine.channel.TSBChannelManager;
import com.tuisongbao.engine.channel.message.TSBSubscribeMessage;
import com.tuisongbao.engine.channel.message.TSBUnsubscribeMessage;
import com.tuisongbao.engine.common.EventEmitter;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.TSBEngineBindCallback;
import com.tuisongbao.engine.common.TSBEngineCallback;
import com.tuisongbao.engine.log.LogUtil;

import org.json.JSONException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TSBChannel extends EventEmitter {
    private static final String TAG = TSBChannel.class.getSimpleName();

    protected TSBChannelManager mChannelManager;

    /**
     * This field must be channel, because when serialize message, this will be parse into it's name string.
     */
    protected String channel;
    transient ConcurrentMap<String, CopyOnWriteArrayList<TSBEngineBindCallback>> eventHandlers = new ConcurrentHashMap<String, CopyOnWriteArrayList<TSBEngineBindCallback>>();

    public TSBChannel(String name, TSBChannelManager channelManager) {
        mChannelManager = channelManager;
        this.channel = name;
    }

    public String getName() {
        return channel;
    }

    public void setName(String name) {
        channel = name;
    }

    public void subscribe() {
        LogUtil.debug(LogUtil.LOG_TAG_CHANNEL, "Begin auth channel: " + channel);
        validate(new TSBEngineCallback<String>() {

            @Override
            public void onSuccess(String t) {
                LogUtil.info(LogUtil.LOG_TAG_CHANNEL, "Channel validation pass: " + t);
                try {
                    sendSubscribeRequest();
                } catch (Exception e) {
                    LogUtil.error(TAG, "Send subscribe request failed", e);
                }
            }

            @Override
            public void onError(int code, String message) {
                LogUtil.info(LogUtil.LOG_TAG_CHANNEL, "Channel validation failed: " + message);
                handleErrorMessage(formatEventName(Protocol.CHANNEL_NAME_SUBSCRIPTION_ERROR), message);

                // remove reference from tsbchannel manager
                mChannelManager.unsubscribe(channel);
            }
        });
    }

    public void unsubscribe() {
        try {
            TSBUnsubscribeMessage message = new TSBUnsubscribeMessage();
            TSBChannel data = new TSBChannel(channel, mChannelManager);
            message.setData(data);
            mChannelManager.send(message, null);

            // Remove listeners on engineIO layer
            mChannelManager.unbind(channel);

            eventHandlers = new ConcurrentHashMap<>();
        } catch (Exception e) {

        }
    }

    protected void sendSubscribeRequest() throws JSONException {
        TSBSubscribeMessage message = generateSubscribeMessage();
        mChannelManager.send(message, null);
    }

    protected void validate(TSBEngineCallback<String> callback) {
        callback.onSuccess(channel);
    }

    protected TSBSubscribeMessage generateSubscribeMessage() {
        TSBSubscribeMessage message = new TSBSubscribeMessage();
        // As TSBPresenceChannel has all properties, so use it to be the event data.
        TSBPresenceChannel data = new TSBPresenceChannel(channel, mChannelManager);
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
