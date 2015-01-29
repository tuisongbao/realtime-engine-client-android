package com.tuisongbao.android.engine.entity;

import com.tuisongbao.android.engine.engineio.EngineConstants;

public class TSBEngineConstants {

    // channel
    public static final String TSBENGINE_EVENT_UNBIND = "unbund";
    public static final String TSBENGINE_CHANNEL_PREFIX_PRIVATE = "private-";
    public static final String TSBENGINE_CHANNEL_PREFIX_PRESENCE = "presence-";

    // connection
    public static final int TSBENGINE_CODE_SUCCESS = EngineConstants.ENGINE_CODE_SUCCESS;
    
    // channel code
    /**
     * the code is triggered by client
     */
    public static final int CHANNEL_CODE_INVALID_OPERATION_ERROR = EngineConstants.CHANNEL_CODE_INVALID_OPERATION_ERROR;
    public static final int CONNECTION_CODE_CONNECTION_SEND_MESSAGE_FAILED = EngineConstants.CONNECTION_CODE_CONNECTION_SEND_MESSAGE_FAILED;
    
    // channel name
    public static final String CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED = EngineConstants.CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED;
    public static final String CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED_ERROR = EngineConstants.CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED_ERROR;
    
    // chat code
    public static final int CHAT_CODE_LOGIN_FAILED = -3001;

    // bind name
    public static final String TSBENGINE_BIND_NAME_CONNECTION_CONNECTED = EngineConstants.EVENT_CONNECTION_CHANGE_STATUS;
    public static final String TSBENGINE_BIND_NAME_CHAT_PRESENCE_CHANGED = "engine_chat:user:presenceChanged";
    /**
     * 当重复登陆时会返回该code
     */
    public static final int CHAT_CODE_LOGIN_HAS_LOGINED = -3002;

    private TSBEngineConstants() {
        // empty
    }
}
