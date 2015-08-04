package com.tuisongbao.engine.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Protocol {

    // request key
    public static final String REQUEST_KEY_WS_ADDR = "addr";
    public static final String REQUEST_KEY_CODE = "code";
    public static final String REQUEST_KEY_ERROR_MESSAGE = "message";

    // common
    public static final String EVENT_NAME_PATTERN_VALID = "^engine_";
    public static final String EVENT_NAME_PATTERN_CONNECTION = "^engine_connection";
    public static final String EVENT_NAME_PATTERN_CHANNEL = "^engine_channel";
    public static final String EVENT_NAME_PATTERN_CHAT = "^engine_chat";
    public static final String EVENT_NAME_PATTERN_RESPONSE = "^engine_response$";

    public static final String EVENT_NAME_MESSAGE_NEW = "engine_chat:message:new";
    public static final String EVENT_NAME_USER_PRESENCE_CHANGE = "engine_chat:user:presenceChanged";

    // connection
    public static final String EVENT_NAME_CONNECTION_ESTABLISHED = "engine_connection:established";
    public static final String EVENT_NAME_CONNECTION_ERROR = "engine_connection:error";

    // connection strategy
    /**
     * static ：以静态的间隔进行重连，服务端可以通过 engine_connection:error ConnectionEvent 的
     * data.reconnectStrategy 来启用，通过 data.reconnectIn 设置重连间隔。
     */
    public static final String CONNECTION_STRATEGY_STATIC = "static";
    /**
     * backoff ：默认策略，重连间隔从一个基数开始（默认为 0），每次乘以 2 ，直到达到最大值（默认为 10 秒）。服务端可以通过
     * engine_connection:error ConnectionEvent 的 data.reconnectIn 、 data.reconnectInMax
     * 来调整基数和最大值，当然对应的 data.reconnectStrategy 需为 backoff 。
     *
     * 以默认值为例，不断自动重连时，间隔将依次为（单位毫秒）：0 1 2 4 8 16 64 128 256 1024 2048 4096 8192
     * 10000 10000 ... 。
     */
    public static final String CONNECTION_STRATEGY_BACKOFF = "backoff";

    /**
     * backoff default reconnection
     */
    public static final int CONNECTION_STRATEGY_BACKOFF_DEFAULT_RECONNECTIN = 0;
    /**
     * backoff default reconnection
     */
    public static final int CONNECTION_STRATEGY_BACKOFF_DEFAULT_RECONNECTINMAX = 10000;
    /**
     * 禁止连接，出现4000 ~ 4099（连接将被服务端关闭, 客户端 不 应该进行重连）时
     */
    public static final int CONNECTION_STRATEGY_CONNECTION_TYPE_FORBIDDEN_CONNECTION = 1;
    /**
     * 按策略重新连接，出现4100 ~ 4199（连接将被服务端关闭, 客户端应按照指示进行重连）时
     */
    public static final int CONNECTION_STRATEGY_CONNECTION_TYPE_RECONNECTION_BY_STRATEGY = 2;
    /**
     * 按策略重新连接，出现4200 ~ 4299（连接将被服务端关闭, 客户端应立即重连）时
     */
    public static final int CONNECTION_STRATEGY_CONNECTION_TYPE_RECONNECTION_IMMEDIATELY = 3;

    public static final String CHANNEL_NAME_SUBSCRIPTION_ERROR = "engine_channel:subscription_error";

    public static boolean isValidEvent(String eventName) {
        Pattern pat = Pattern.compile(EVENT_NAME_PATTERN_VALID, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pat.matcher(eventName);
        return matcher.find();
    }

    public static boolean isConnectionEvent(String eventName) {
        Pattern pat = Pattern.compile(EVENT_NAME_PATTERN_CONNECTION, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pat.matcher(eventName);
        return matcher.find();
    }

    public static boolean isChannelEvent(String eventName) {
        Pattern pat = Pattern.compile(EVENT_NAME_PATTERN_CHANNEL, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pat.matcher(eventName);
        return matcher.find();
    }

    public static boolean isServerResponseEvent(String eventName) {
        Pattern pat = Pattern.compile(EVENT_NAME_PATTERN_RESPONSE, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pat.matcher(eventName);
        return matcher.find();
    }

    public static boolean isChatEvent(String eventName) {
        Pattern pat = Pattern.compile(EVENT_NAME_PATTERN_CHAT, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pat.matcher(eventName);
        return matcher.find();
    }
}
