package com.tuisongbao.engine.engineio.source;

import com.tuisongbao.engine.common.EventEmitter;
import com.tuisongbao.engine.engineio.pipeline.IEnginePipeline;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A common parent for all engine data sources.
 *
 */
public class BaseEngineDataSource extends EventEmitter implements IEngineDataSource {
    private IEnginePipeline mPipeline;
    private final Lock mCallbackLock = new ReentrantLock();
    private final Condition mCallbackChanged = mCallbackLock.newCondition();

    public BaseEngineDataSource() {
        // TODO: 15-8-1 Figure out the difference with BaseEngineIODataSource and can remove one of them??
    }

    /**
     * Construct a new instance and set the onResponse.
     *
     * @param callback
     *            An object implementing the IEnginePipeline interface that
     *            should receive data from this source.
     */
    public BaseEngineDataSource(IEnginePipeline callback) {
        setCallback(callback);
    }

    /**
     * Set the current source onResponse to the given value.
     *
     * @param callback
     *            a valid onResponse or null if you wish to stop the source from
     *            sending updates.
     */
    public void setCallback(IEnginePipeline callback) {
        mCallbackLock.lock();
        mPipeline = callback;
        mCallbackChanged.signal();
        mCallbackLock.unlock();
    }

    /**
     * Clear the onResponse so no further updates are sent.
     *
     * Subclasses should be sure to call super.stop() so they also stop sending
     * updates when killed by a user.
     */
    public void stop() {
        setCallback(null);
    }

    protected void dispatchEvent(String eventString) {
        if (mPipeline != null) {
            mPipeline.ferry(eventString);
        }
    }
}
