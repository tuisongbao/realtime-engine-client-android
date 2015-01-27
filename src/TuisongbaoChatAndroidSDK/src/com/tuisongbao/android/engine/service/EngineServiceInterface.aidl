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
         * @param listener
         * @return
         */
        boolean send(in RawMessage message, in EngineServiceListener listener);

        /**
         * 添加消息源
         * 
         * @param appId
         * @return
         */
        void addEngineInterface(String appId);

        /**
         * 绑定事件
         * 
         * @param message
         * @param listener
         * @return
         */
        void bind(in RawMessage message, in EngineServiceListener listener);

        /**
         * 解除绑定事件
         * 
         * @param message
         * @param listener
         * @return
         */
        void unbind(in RawMessage message, in EngineServiceListener listener);
}
