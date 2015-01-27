package com.tuisongbao.android.engine.engineio.source;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.util.Log;

import com.tuisongbao.android.engine.engineio.exception.DataSourceException;

/**
 * Common functionality for data sources that read a stream of newline-separated
 * messages in a separate thread from the main activity.
 */
public abstract class BaseEngineIODataSource extends ContextualEngineDataSource
        implements Runnable {
    private final static String TAG = BaseEngineIODataSource.class.getSimpleName();
    private boolean mRunning = false;
    private final Lock mConnectionLock = new ReentrantLock();
    protected final Condition mConnectionChanged = mConnectionLock.newCondition();

    public BaseEngineIODataSource(IEngineCallback callback, Context context) {
        super(callback, context);
    }

    public BaseEngineIODataSource(Context context) {
        this(null, context);
    }

    public synchronized void start() {
        if(!mRunning) {
            mRunning = true;
            new Thread(this).start();
        }
    }

    public synchronized void stop() {
        super.stop();
        if(!mRunning) {
            Log.d(getTag(), "Already stopped.");
            return;
        }
        Log.d(getTag(), "Stopping " + getTag() + " source");
        mRunning = false;
        disconnect();
    }

    public void run() {
        while(mRunning) {
            mConnectionLock.lock();

            try {
                waitForConnection();
            } catch(DataSourceException e) {
                Log.i(getTag(), "Unable to connect to target engine -- " +
                        "sleeping for awhile before trying again");
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException e2){
                    stop();
                }
                mConnectionLock.unlock();
                continue;
            } catch(InterruptedException e) {
                stop();
                mConnectionLock.unlock();
                continue;
            }
            try {
                // waiting for connection
                mConnectionChanged.await();
            } catch(InterruptedException e) {
                Log.d(TAG, "Interrupted while waiting for a new " +
                        "item for notification -- likely shutting down");
                stop();
                mConnectionLock.unlock();
                continue;
            }

            mConnectionLock.unlock();
        }
        Log.d(getTag(), "Stopped " + getTag());
    }

    protected void reconnect() {
        mConnectionLock.lock();
        mConnectionChanged.signal();
        mConnectionLock.unlock();
    }

    protected boolean isRunning() {
        return mRunning;
    }

    protected void lockConnection() {
        mConnectionLock.lock();
    }

    protected void unlockConnection() {
        mConnectionLock.unlock();
    }

    protected Condition createCondition() {
        return mConnectionLock.newCondition();
    }

    /**
     * If not already connected to the data source, initiate the connection and
     * block until ready to be read.
     *
     * @throws DataSourceException The connection is still alive, but it
     *      returned an unexpected result that cannot be handled.
     * @throws InterruptedException if the interrupted while blocked -- probably
     *      shutting down.
     */
    protected abstract void waitForConnection() throws DataSourceException, InterruptedException;

    /**
     * Perform any cleanup necessary to disconnect from the interface.
     */
    protected abstract void disconnect();
};
