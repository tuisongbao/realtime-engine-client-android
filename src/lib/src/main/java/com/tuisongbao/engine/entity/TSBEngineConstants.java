package com.tuisongbao.engine.entity;

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

    // channel name
    public static final String CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED = Protocol.CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED;
    public static final String CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED_ERROR = Protocol.CHANNEL_NAME_SUBSCRIPTION_ERROR;

    // bind name
    public static final String TSBENGINE_BIND_NAME_CONNECTION_CONNECTED = Protocol.EVENT_CONNECTION_CHANGE_STATUS;
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
