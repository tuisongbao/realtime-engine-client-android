package com.tuisongbao.android.engine.channel.entity;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.channel.message.TSBSubscribeMessage;
import com.tuisongbao.android.engine.channel.message.TSBUnsubscribeMessage;
import com.tuisongbao.android.engine.common.TSBEngineBindCallback;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.common.TSBResponseMessage;
import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBChannel {
    /**
     * This field must be channel, because when serialize message, this will be parse into it's name string.
     */
    String channel;
    ConcurrentMap<String, CopyOnWriteArrayList<TSBEngineBindCallback>> eventHandlers = new ConcurrentHashMap<String, CopyOnWriteArrayList<TSBEngineBindCallback>>();
    TSBEngineBindCallback bindCallback = new TSBEngineBindCallback() {

        @Override
        public void onEvent(String channelName, String eventName, String data) {
            LogUtil.info(LogUtil.LOG_TAG_CHANNEL, channelName + " Got " + eventName + " with data " + data);
            if (!StrUtil.isEqual(channelName, channel)) {
                return;
            }
            eventName = formatEventName(eventName);
            CopyOnWriteArrayList<TSBEngineBindCallback> handlers = eventHandlers.get(eventName);
            if (handlers == null) {
                return;
            }
            LogUtil.info(LogUtil.LOG_TAG_CHANNEL, "There are" + handlers.size() + " handlers and begine to call them");
            for (TSBEngineBindCallback handler : handlers) {
                handler.onEvent(channelName, eventName, data);
            }
        }
    };

    public TSBChannel(String name) {
        this.channel = name;
    }

    public String getName() {
        return channel;
    }

    public void setName(String name) {
        channel = name;
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
        CopyOnWriteArrayList<TSBEngineBindCallback> list = eventHandlers.get(eventName);
        if (list == null || list.isEmpty()) {
            return;
        }
        if (callback == null) {
            eventHandlers.remove(eventName);
            return;
        }
        for (TSBEngineBindCallback local : list) {
            if (local == callback) {
                list.remove(local);
            }
        }
    }

    public void subscribe() {
        LogUtil.info(LogUtil.LOG_TAG_CHANNEL, "Bind event listner for channel: " + channel);
        TSBResponseMessage response = new TSBResponseMessage();
        response.setCallback(bindCallback);
        TSBEngine.bind(channel, response);

        LogUtil.debug(LogUtil.LOG_TAG_CHANNEL, "Begin auth channel: " + channel);
        validata(new TSBEngineCallback<String>() {

            @Override
            public void onSuccess(String t) {
                LogUtil.info(LogUtil.LOG_TAG_CHANNEL, "Channel validation pass: " + t);
                TSBSubscribeMessage message = generateSubscribeMessage();
                TSBEngine.send(message.getName(), message.serialize(), null);
            }

            @Override
            public void onError(int code, String message) {
                LogUtil.info(LogUtil.LOG_TAG_CHANNEL, "Channel validation failed: " + message);
                handleErrorMessage(formatEventName(EngineConstants.CHANNEL_NAME_SUBSCRIPTION_ERROR), message);
            }
        });
    }

    public void unsubscribe() {
        TSBUnsubscribeMessage message = new TSBUnsubscribeMessage();
        TSBChannel data = new TSBChannel(channel);
        message.setData(data);
        TSBEngine.send(message.getName(), message.serialize(), null);

        // Remove listeners on engineIO layer
        TSBEngine.unbind(channel);

        eventHandlers = null;
    }

    protected void validata(TSBEngineCallback<String> callback) {
        callback.onSuccess(channel);
    }

    protected TSBSubscribeMessage generateSubscribeMessage() {
        TSBSubscribeMessage message = new TSBSubscribeMessage();
        TSBPresenceChannel data = new TSBPresenceChannel(channel);
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
