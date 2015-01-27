package com.tuisongbao.android.engine.service;

import com.tuisongbao.android.engine.service.RawMessage;

/**
 * The interface for receiving a message update callback from the
 * EngineService over AIDL.
 */
oneway interface EngineServiceListener {
    void call(in RawMessage value);
}
