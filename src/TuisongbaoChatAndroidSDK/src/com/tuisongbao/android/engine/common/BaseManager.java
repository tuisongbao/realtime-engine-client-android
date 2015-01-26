package com.tuisongbao.android.engine.common;

import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.TSBEngine.TSBEngineListener;

public abstract class BaseManager {

    protected boolean send(ITSBMessage message) {
        return send(message, null);
    }

    protected boolean send(ITSBMessage message, TSBEngineListener l) {
        return TSBEngine.send(message.getName(), message.getData(), l);
    }
}
