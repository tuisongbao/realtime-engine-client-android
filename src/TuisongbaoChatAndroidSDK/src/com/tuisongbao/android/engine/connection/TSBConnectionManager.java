package com.tuisongbao.android.engine.connection;

import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.connection.entity.TSBConnection;
import com.tuisongbao.android.engine.connection.message.TSBConnectionResponseMessage;
import com.tuisongbao.android.engine.entity.TSBEngineConstants;

public class TSBConnectionManager {

    private static TSBConnectionManager mInstance;
    private TSBConnectionManager() {
        // empty
    }
    
    public static TSBConnectionManager getInstance() {
        if (mInstance == null) {
            mInstance = new TSBConnectionManager();
        }
        return mInstance;
    }
    
    public void bindConnectionChangeStatusEvent(TSBEngineCallback<TSBConnection> callback) {
        TSBConnectionResponseMessage message = new TSBConnectionResponseMessage();
        message.setCallback(callback);
        TSBEngine.bind(TSBEngineConstants.TSBENGINE_BIND_NAME_CONNECTION_CONNECTED, message);
    }
}
