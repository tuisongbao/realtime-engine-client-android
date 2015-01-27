package com.tuisongbao.android.engine.engineio.source;

import com.tuisongbao.android.engine.service.RawMessage;

/**
 * 从一个数据源接受数据.
 * 
 */
public interface IEngineCallback {
    /**
     * 接受新消息
     *
     * @param message 新消息.
     */
    public void receive(RawMessage message);
}
