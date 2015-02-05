package com.tuisongbao.android.engine.engineio.sink;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;

import com.tuisongbao.android.engine.engineio.exception.DataSinkException;
import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.service.RawMessage;

/**
 * Functionality to notify multiple clients asynchronously of new message.
 *
 */
public abstract class BaseEngineCallbackSink implements IEngineDataSink {
    private final static String TAG = BaseEngineCallbackSink.class.getSimpleName();

    private NotificationThread mNotificationThread = new NotificationThread();
    private Lock mNotificationsLock = new ReentrantLock();
    private Condition mNotificationReceived = mNotificationsLock.newCondition();
    /**
     * We should make received message in order
     */
    private BlockingQueue<RawMessage> mNotifications =
            new LinkedBlockingQueue<RawMessage>();

    public BaseEngineCallbackSink() {
        mNotificationThread.start();
    }

    public synchronized void stop() {
        mNotificationThread.done();
    }

    public boolean receive(RawMessage message)
            throws DataSinkException {
        mNotificationsLock.lock();
        mNotifications.offer(message);
        mNotificationReceived.signal();
        mNotificationsLock.unlock();
        return true;
    }

    abstract protected void propagateMessage(RawMessage message);

    private class NotificationThread extends Thread {
        private boolean mRunning = true;

        private synchronized boolean isRunning() {
            return mRunning;
        }

        public synchronized void done() {
            LogUtil.debug(LogUtil.LOG_TAG_ENGINEIO, "Stopping notification thread");
            mRunning = false;
            interrupt();
        }

        public void run() {
            while(isRunning()) {
                mNotificationsLock.lock();
                try {
                    if(mNotifications.isEmpty()) {
                        mNotificationReceived.await();
                    }
                } catch(InterruptedException e) {
                    LogUtil.debug(LogUtil.LOG_TAG_ENGINEIO, "Interrupted while waiting for a new " +
                            "item for notification -- likely shutting down");
                    return;
                } finally {
                    mNotificationsLock.unlock();
                }


                RawMessage message = null;
                Log.d(TAG, "Received event number: " + mNotifications.size());
                while((message = mNotifications.poll()) != null) {
                    propagateMessage(message);
                }
            }
            LogUtil.debug(LogUtil.LOG_TAG_ENGINEIO, "Stopped message receive");
        }
    }
}
