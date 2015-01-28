package com.tuisongbao.android.engine.connection;

import com.tuisongbao.android.engine.common.BaseManager;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.connection.entity.TSBConnection;
import com.tuisongbao.android.engine.connection.message.TSBConnectionResponseMessage;

public class TSBConnectionManager extends BaseManager {

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
    
    public void bind(String bindName, TSBEngineCallback<TSBConnection> callback) {
        TSBConnectionResponseMessage message = new TSBConnectionResponseMessage();
        message.setCallBack(callback);
        bind(bindName, callback);
    }
}
