package com.tuisongbao.android.engine.service;

import com.tuisongbao.android.engine.service.RawMessage;

/**
 * The interface for receiving a measurement update callback from the
 * VehicleService over AIDL.
 */
oneway interface EngineServiceListener {
    void call(in RawMessage value);
}
