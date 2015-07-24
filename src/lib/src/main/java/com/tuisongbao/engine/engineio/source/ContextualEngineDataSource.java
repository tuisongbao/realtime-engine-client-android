package com.tuisongbao.engine.engineio.source;

import android.content.Context;

/**
 * A parent class for data sources that require access to an Android context.
 */
public abstract class ContextualEngineDataSource extends BaseEngineDataSource {
    private Context mContext;
    private WakeLockManager mWakeLocker;

    public ContextualEngineDataSource(Context context) {
        this(null, context);
    }

    public ContextualEngineDataSource(IEngineCallback callback,
            Context context) {
        super(callback);
        mContext = context;
        mWakeLocker = new WakeLockManager(getContext(), getTag());
    }

    protected Context getContext() {
        return mContext;
    }

    /**
     * The data source is connected, so if necessary, keep the device awake.
     */
    protected void connected() {
        mWakeLocker.acquireWakeLock();
    }

    /**
     * The data source is connected, so if necessary, let the device go to
     * sleep.
     */
    protected void disconnected() {
        mWakeLocker.releaseWakeLock();
    }
}
