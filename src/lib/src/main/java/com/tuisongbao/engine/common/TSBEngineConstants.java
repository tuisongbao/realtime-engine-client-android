package com.tuisongbao.engine.common;

import com.tuisongbao.engine.common.Protocol;

public class TSBEngineConstants {

    // channel
    public static final String TSBENGINE_EVENT_UNBIND = "unbund";
    public static final String TSBENGINE_CHANNEL_PREFIX_PRIVATE = "private-";
    public static final String TSBENGINE_CHANNEL_PREFIX_PRESENCE = "presence-";

    // channel code
    /**
     * the code is triggered by client
     */
    public static final int CHANNEL_CODE_INVALID_OPERATION_ERROR = Protocol.CHANNEL_CODE_INVALID_OPERATION_ERROR;
    public static final int CONNECTION_CODE_CONNECTION_SEND_MESSAGE_FAILED = Protocol.CONNECTION_CODE_CONNECTION_SEND_MESSAGE_FAILED;

    // bind name
    public static final String TSBENGINE_BIND_NAME_CHAT_PRESENCE_CHANGED = "engine_chat:user:presenceChanged";


    // code
    // common
    public static final int TSBENGINE_CODE_SUCCESS = Protocol.ENGINE_CODE_SUCCESS;
    /**
     * 当未登陆而去操作需要登录的操作时返回
     */
    public static final int TSBENGINE_CODE_PERMISSION_DENNY = -9002;
    /**
     * 传递参数不合法时返回
     */
    public static final int TSBENGINE_CODE_ILLEGAL_PARAMETER = -9003;
    /**
     * 当未登陆而去操作需要登录的操作时返回
     */
    public static final int TSBENGINE_CODE_NETWORK_UNAVAILABLE = -9004;
    // chat code
    /**
     * 登录失败
     */
    public static final int TSBENGINE_CHAT_CODE_LOGIN_FAILED = -3001;
    /**
     * 当重复登录时会返回
     */
    public static final int TSBENGINE_CHAT_CODE_LOGIN_HAS_LOGINED = -3002;

    private TSBEngineConstants() {
        // empty
    }
}
