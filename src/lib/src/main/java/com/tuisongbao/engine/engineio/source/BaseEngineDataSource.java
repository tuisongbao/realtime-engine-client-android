package com.tuisongbao.engine.engineio.source;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.tuisongbao.engine.service.RawMessage;

/**
 * A common parent for all engine data sources.
 * 
 */
public class BaseEngineDataSource implements IEngineDataSource {
    private final static String TAG = "BaseEngineDataSource";
    private IEngineCallback mCallback;
    private final Lock mCallbackLock = new ReentrantLock();
    private final Condition mCallbackChanged = mCallbackLock.newCondition();

    public BaseEngineDataSource() {
    }

    /**
     * Construct a new instance and set the callback.
     * 
     * @param callback
     *            An object implementing the IEngineCallback interface that
     *            should receive data from this source.
     */
    public BaseEngineDataSource(IEngineCallback callback) {
        setCallback(callback);
    }

    /**
     * Set the current source callback to the given value.
     * 
     * @param callback
     *            a valid callback or null if you wish to stop the source from
     *            sending updates.
     */
    public void setCallback(IEngineCallback callback) {
        mCallbackLock.lock();
        mCallback = callback;
        mCallbackChanged.signal();
        mCallbackLock.unlock();
    }

    /**
     * Clear the callback so no further updates are sent.
     * 
     * Subclasses should be sure to call super.stop() so they also stop sending
     * updates when killed by a user.
     */
    public void stop() {
        setCallback(null);
    }

    /**
     * Pass a new message to the callback, if set.
     * 
     * @param message
     *            the new message object.
     */
    protected void handleMessage(RawMessage message) {
        if (mCallback != null && message != null) {
            mCallback.receive(message);
        }
    }

    /**
     * Return a string suitable as a tag for logging.
     */
    protected String getTag() {
        return TAG;
    }
}
