package com.tuisongbao.android.engine.entity;

import com.tuisongbao.android.engine.engineio.EngineConstants;

public class TSBEngineConstants {

    // channel
    public static final String TSBENGINE_EVENT_UNBIND = "unbund";
    public static final String TSBENGINE_CHANNEL_PREFIX_PRIVATE = "private-";
    public static final String TSBENGINE_CHANNEL_PREFIX_PRESENCE = "presence-";

    // connection
    public static final int TSBENGINE_CONNECTION_CODE_SUCCESS = EngineConstants.CONNECTION_CODE_SUCCESS;

    // connection bind name
    public static final String TSBENGINE_BIND_NAME_CONNECTION_CONNECTED = EngineConstants.EVENT_CONNECTION_CHANGE_STATUS;
    
    // channel code
    /**
     * the code is triggered by client
     */
    public static final int CHANNEL_CODE_INVALID_OPERATION_ERROR = EngineConstants.CHANNEL_CODE_INVALID_OPERATION_ERROR;
    
    // channel name
    public static final String CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED = EngineConstants.CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED;
    public static final String CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED_ERROR = EngineConstants.CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED_ERROR;

    private TSBEngineConstants() {
        // empty
    }
}
