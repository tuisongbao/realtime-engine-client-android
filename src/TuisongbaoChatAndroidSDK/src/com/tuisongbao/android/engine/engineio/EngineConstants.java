package com.tuisongbao.android.engine.engineio;

import org.json.JSONException;
import org.json.JSONObject;

import com.tuisongbao.android.engine.util.StrUtil;

public class EngineConstants {

    // request key
    public static final String REQUEST_KEY_WS_ADDR = "addr";
    public static final String REQUEST_KEY_NAME = "name";
    public static final String REQUEST_KEY_ID = "id";
    public static final String REQUEST_KEY_CHANNEL = "channel";
    public static final String REQUEST_KEY_DATA = "data";
    public static final String REQUEST_KEY_CODE = "code";
    public static final String REQUEST_KEY_ERROR_MESSAGE = "message";
    
    // connection
    public static final String CONNECTION_NAME_CONNECTION_SUCCEEDED = "engine_connection:established";
    public static final String CONNECTION_NAME_CONNECTION_SUCCEEDED_ERROR = "engine_connection:error";
    public static final String CONNECTION_PREFIX = "engine_connection:";
    public static final String CONNECTION_CONNECTED = "established";
    public static final String CONNECTION_ERROR = "error";
    public static final int CONNECTION_STATUS_CONNECTED = 1;
    public static final int CONNECTION_STATUS_ERROR = 2;
    public static final int CONNECTION_STATUS_CLOSED = 3;
    public static final int CONNECTION_STATUS_DISCONNECTED = 4;
    public static final int CONNECTION_STATUS_CONNECTING = 5;
    public static final int CONNECTION_STATUS_NONE = 0;
    
    // channel name
    public static final String CHANNEL_NAME_PREFIX = "engine_channel";
    public static final String CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED = "engine_channel:subscription_succeeded";
    public static final String CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED_ERROR = "engine_channel:subscription_error";
    public static final String CHANNEL_NAME_UNSUBSCRIPTION_SUCCEEDED = "engine_channel:unsubscription_succeeded";
    public static final String CHANNEL_NAME_UNSUBSCRIPTION_SUCCEEDED_ERROR = "engine_channel:unsubscription_error";
    
    // connection code
    public static final int CONNECTION_CODE_SUCCESS = 0;
    public static final int CONNECTION_CODE_CONNECTION_CLOSED = -1001;
    public static final int CONNECTION_CODE_CONNECTION_EXCEPTION = -1002;
    public static final int CONNECTION_CODE_CONNECTION_SEND_MESSAGE_FAILED = -1003;
    
    // channel code
    public static final int CHANNEL_CODE_INVALID_OPERATION_ERROR = -2001;
    
    // bind name
    public static final String EVENT_CONNECTION_CHANGE_STATUS = "android:engine_connection:connection_change_status";
    
    public static int getConnectionStatus(String src) {
        String statusString = getValue(src, CONNECTION_PREFIX);
        if (StrUtil.isEmpty(statusString)) {
            return CONNECTION_STATUS_NONE;
        } else if (statusString.endsWith(CONNECTION_CONNECTED)) {
            return CONNECTION_STATUS_CONNECTED;
        } else if (statusString.endsWith(CONNECTION_ERROR)) {
            return CONNECTION_STATUS_ERROR;
        } else {
            return CONNECTION_STATUS_NONE;
        }
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
