package com.tuisongbao.android.engine.service;

import com.tuisongbao.android.engine.service.RawMessage;
import com.tuisongbao.android.engine.service.EngineServiceListener;
/**
 * The AIDL interface for a EngineService running in a separate process.
 * 
 */
interface EngineServiceInterface {

        /**
         * 发送消息到事实引擎
         * 
         * @param message
         * @return
         */
        boolean send(in RawMessage message, in EngineServiceListener l);
}
