package com.tuisongbao.engine.common;

import org.json.JSONException;
import org.json.JSONObject;

import com.tuisongbao.engine.util.StrUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Protocol {

    // request key
    public static final String REQUEST_KEY_WS_ADDR = "addr";
    public static final String REQUEST_KEY_NAME = "name";
    public static final String REQUEST_KEY_ID = "id";
    public static final String REQUEST_KEY_CHANNEL = "channel";
    public static final String REQUEST_KEY_DATA = "data";
    public static final String REQUEST_KEY_CODE = "code";
    public static final String REQUEST_KEY_ERROR_MESSAGE = "message";
    public static final String REQUEST_KEY_RESPONSE_OK = "ok";
    public static final String REQUEST_KEY_RESPONSE_RESULT = "result";
    public static final String REQUEST_KEY_RESPONSE_ERROR = "error";
    public static final String REQUEST_KEY_RESPONSE_TO = "to";
    public static final String REQUEST_KEY_RECONNECTION_STRATEGY = "reconnectStrategy";
    public static final String REQUEST_KEY_RECONNECTION_IN = "reconnectIn";
    public static final String REQUEST_KEY_RECONNECTION_INMAX = "reconnectInMax";

    // common
    public static final String EVENT_NAME_PATTERN_INTERNAL = "^engine_";
    public static final String EVENT_NAME_PATTERN_CONNECTION = "^engine_connection";
    public static final String EVENT_NAME_PATTERN_CHANNEL = "^engine_channel";
    public static final String EVENT_NAME_PATTERN_RESPONSE = "^engine_response$";

    // connection
    public static final String EVENT_NAME_CONNECTION_ESTABLISHED = "engine_connection:established";
    public static final String EVENT_NAME_CONNECTION_ERROR = "engine_connection:error";

    public static final int CONNECTION_STATUS_CONNECTED = 1;
    public static final int CONNECTION_STATUS_ERROR = 2;
    public static final int CONNECTION_STATUS_CLOSED = 3;
    public static final int CONNECTION_STATUS_DISCONNECTED = 4;
    public static final int CONNECTION_STATUS_CONNECTING = 5;
    public static final int CONNECTION_STATUS_NONE = 0;

    // connection strategy
    /**
     * static ：以静态的间隔进行重连，服务端可以通过 engine_connection:error Event 的
     * data.reconnectStrategy 来启用，通过 data.reconnectIn 设置重连间隔。
     */
    public static final String CONNECTION_STRATEGY_STATIC = "static";
    /**
     * backoff ：默认策略，重连间隔从一个基数开始（默认为 0），每次乘以 2 ，直到达到最大值（默认为 10 秒）。服务端可以通过
     * engine_connection:error Event 的 data.reconnectIn 、 data.reconnectInMax
     * 来调整基数和最大值，当然对应的 data.reconnectStrategy 需为 backoff 。
     *
     * 以默认值为例，不断自动重连时，间隔将依次为（单位毫秒）：0 1 2 4 8 16 64 128 256 1024 2048 4096 8192
     * 10000 10000 ... 。
     */
    public static final String CONNECTION_STRATEGY_BACKOFF = "backoff";
    /**
     * static default reconnection
     */
    public static final int CONNECTION_STRATEGY_STATIC_DEFAULT_RECONNECTIN = 10000;
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

    // channel name
    public static final String CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED = "engine_channel:subscription_succeeded";
    public static final String CHANNEL_NAME_SUBSCRIPTION_ERROR = "engine_channel:subscription_error";
    public static final String CHANNEL_NAME_UNSUBSCRIPTION_SUCCEEDED = "engine_channel:unsubscription_succeeded";
    public static final String CHANNEL_NAME_UNSUBSCRIPTION_SUCCEEDED_ERROR = "engine_channel:unsubscription_error";

    // chat name
    public static final String CHAT_NAME_NEW_MESSAGE = "engine_chat:message:new";

    // common code
    public static final int ENGINE_CODE_UNKNOWN = -9001;
    public static final int ENGINE_CODE_SUCCESS = 0;
    public static final int ENGINE_CODE_INVALID_OPERATION = -9002;
    public static final String ENGINE_MESSAGE_UNKNOWN_ERROR = "TuiSongBao internal error, please contact us";
    public static final int CONNECTION_CODE_CONNECTION_SEND_MESSAGE_FAILED = -1003;

    // channel code
    public static final int CHANNEL_CODE_INVALID_OPERATION_ERROR = -2001;

    // bind name
    public static final String EVENT_CONNECTION_CHANGE_STATUS = "engine_connection:connection_change_status";

    public static JSONObject parseEvent(String eventString) throws JSONException {
        return new JSONObject(eventString);
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

    public static String genErrorJsonString(int code, String message) {
        JSONObject json = new JSONObject();
        try {
            json.put(REQUEST_KEY_CODE, code);
            json.put(REQUEST_KEY_ERROR_MESSAGE, StrUtil.strNotNull(message));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }


    private static final String getValue(String src, String prefix) {
        if (StrUtil.isEmpty(src)) {
            return "";
        } else if (StrUtil.isEmpty(prefix)) {
            return src;
        } else {
            if (src.startsWith(prefix)) {
                return src.substring(prefix.length());
            } else {
                return src;
            }
        }
    }
}
