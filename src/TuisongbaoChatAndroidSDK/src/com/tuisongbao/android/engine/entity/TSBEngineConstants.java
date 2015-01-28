package com.tuisongbao.android.engine.entity;

import com.tuisongbao.android.engine.engineio.EngineConstants;

public class TSBEngineConstants {
    
    // channel
    public static final String TSBENGINE_EVENT_UNBIND = "unbund";
    public static final String TSBENGINE_CHANNEL_PREFIX_PRIVATE = "private-";
    public static final String TSBENGINE_CHANNEL_PREFIX_PRESENCE = "presence-";
    
    // connection
    public static final int TSBENGINE_CONNECTION_CODE_SUCCESS = EngineConstants.CONNECTION_CODE_SUCCESS;
    
    // event
    public static final String TSBENGINE_EVENT_CONNECTION_STATUS = "event_connect_status";

    private TSBEngineConstants() {
        // empty
    }
}
