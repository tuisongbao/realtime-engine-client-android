package com.tuisongbao.android.engine.engineio;

import com.tuisongbao.android.engine.util.StrUtil;

public class EngineConstants {

    public static final String REQUEST_KEY_WS_ADDR = "addr";
    public static final String REQUEST_KEY_NAME = "name";
    public static final String REQUEST_KEY_ID = "id";
    public static final String REQUEST_KEY_CHANNEL = "channel";
    public static final String REQUEST_KEY_DATA = "data";
    public static final String REQUEST_KEY_CODE = "code";
    public static final String REQUEST_KEY_ERROR_MESSAGE = "message";
    public static final String CONNECTION_PREFIX = "engine_connection:";
    public static final String CONNECTION_CONNECTED = "established";
    public static final String CONNECTION_ERROR = "error";
    public static final int CONNECTION_STATUS_CONNECTED = 1;
    public static final int CONNECTION_STATUS_ERROR = 2;
    public static final int CONNECTION_STATUS_CLOSED = 3;
    public static final int CONNECTION_STATUS_DISCONNECTED = 4;
    public static final int CONNECTION_STATUS_NONE = 0;
    
    // error code
    public static final int ERROR_CODE_SUCCESS = 0;
    
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
