package com.tuisongbao.engine.channel.entity;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.tuisongbao.engine.channel.TSBChannelManager;
import com.tuisongbao.engine.channel.message.TSBSubscribeMessage;
import com.tuisongbao.engine.channel.message.TSBUnsubscribeMessage;
import com.tuisongbao.engine.common.TSBEngineBindCallback;
import com.tuisongbao.engine.common.TSBEngineCallback;
import com.tuisongbao.engine.common.TSBResponseMessage;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.StrUtil;

import org.json.JSONException;

public class TSBChannel {
    private static final String TAG = TSBChannel.class.getSimpleName();

    protected TSBChannelManager mChannelManager;

    /**
     * This field must be channel, because when serialize message, this will be parse into it's name string.
     */
    protected String channel;
    transient ConcurrentMap<String, CopyOnWriteArrayList<TSBEngineBindCallback>> eventHandlers = new ConcurrentHashMap<String, CopyOnWriteArrayList<TSBEngineBindCallback>>();
    transient TSBEngineBindCallback bindCallback = new TSBEngineBindCallback() {

        @Override
        public void onEvent(String channelName, String eventName, String data) {
            LogUtil.info(LogUtil.LOG_TAG_CHANNEL, channelName + " Got " + eventName + " with data " + data);
            if (!StrUtil.isEqual(channelName, channel)) {
                return;
            }
            eventName = formatEventName(eventName);
            CopyOnWriteArrayList<TSBEngineBindCallback> handlers = eventHandlers.get(eventName);
            if (handlers == null) {
                LogUtil.info(LogUtil.LOG_TAG_CHANNEL, "There are no handlers for this event");
                return;
            }
            LogUtil.info(LogUtil.LOG_TAG_CHANNEL, "There are " + handlers.size() + " handlers and begine to call them");
            for (TSBEngineBindCallback handler : handlers) {
                handler.onEvent(channelName, eventName, data);
            }
        }
    };

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

    /***
     * Make sure this method only be called once!
     */
    public void setEventListener() {
        LogUtil.info(LogUtil.LOG_TAG_CHANNEL, "Bind event listner for channel: " + channel);
        TSBResponseMessage response = new TSBResponseMessage();
        response.setCallback(bindCallback);
        mChannelManager.bind(channel, response);
    }

    public void bind(String eventName, TSBEngineBindCallback callback) {
        CopyOnWriteArrayList<TSBEngineBindCallback> list = eventHandlers.get(eventName);
        if (list == null) {
            list = new CopyOnWriteArrayList<TSBEngineBindCallback>();
        }
        list.add(callback);
        eventHandlers.put(eventName, list);
    }

    public void unbind(String eventName, TSBEngineBindCallback callback) {
        if (callback == null) {
            eventHandlers.remove(eventName);
            return;
        }

        CopyOnWriteArrayList<TSBEngineBindCallback> list = eventHandlers.get(eventName);
        if (list == null) {
            eventHandlers.remove(eventName);
            return;
        }
        for (TSBEngineBindCallback local : list) {
            if (local == callback) {
                list.remove(local);
            }
        }

        // If there are no callback for this event, remove it
        list = eventHandlers.get(eventName);
        if (list == null || list.isEmpty()) {
            eventHandlers.remove(eventName);
            return;
        }
    }

    public void subscribe() {
        LogUtil.debug(LogUtil.LOG_TAG_CHANNEL, "Begin auth channel: " + channel);
        validata(new TSBEngineCallback<String>() {

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
            mChannelManager.send(message);

            // Remove listeners on engineIO layer
            mChannelManager.unbind(channel, null);

            eventHandlers = new ConcurrentHashMap<>();
        } catch (Exception e) {

        }
    }

    protected void sendSubscribeRequest() throws JSONException {
        TSBSubscribeMessage message = generateSubscribeMessage();
        mChannelManager.send(message);
    }

    protected void validata(TSBEngineCallback<String> callback) {
        callback.onSuccess(channel);
    }

    protected TSBSubscribeMessage generateSubscribeMessage() {
        TSBSubscribeMessage message = new TSBSubscribeMessage();
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
