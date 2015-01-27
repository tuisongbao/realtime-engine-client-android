package com.tuisongbao.android.engine.engineio.source;


/**
 * 数据源借口
 */
public interface IEngineDataSource {
    /**
     * 设置回调借口，实现数据回调
     */
    public void setCallback(IEngineCallback callback);

    /**
     * 停止数据源接受数据
     */
    public void stop();
}
