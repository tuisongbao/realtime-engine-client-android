package com.tuisongbao.engine.engineio.sink;

import com.tuisongbao.engine.common.Event;
import com.tuisongbao.engine.common.EventEmitter;
import com.tuisongbao.engine.engineio.exception.DataSinkException;
import com.tuisongbao.engine.log.LogUtil;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Functionality to notify multiple clients asynchronously of new event.
 *
 */
public abstract class BaseEngineCallbackSink extends EventEmitter implements IEngineDataSink {
    private final static String TAG = BaseEngineCallbackSink.class.getSimpleName();

    private NotificationThread mNotificationThread = new NotificationThread();
    private Lock mNotificationsLock = new ReentrantLock();
    private Condition mNotificationReceived = mNotificationsLock.newCondition();
    /**
     * We should make received event in order
     */
    private BlockingQueue<Event> mNotifications = new LinkedBlockingQueue<>();

    public BaseEngineCallbackSink() {
        mNotificationThread.start();
    }

    public synchronized void stop() {
        mNotificationThread.done();
    }

    public boolean receive(Event event)
            throws DataSinkException {
        mNotificationsLock.lock();
        mNotifications.offer(event);
        mNotificationReceived.signal();
        mNotificationsLock.unlock();
        return true;
    }

    abstract protected void propagateEvent(Event event);

    private class NotificationThread extends Thread {
        private boolean mRunning = true;

        private synchronized boolean isRunning() {
            return mRunning;
        }

        public synchronized void done() {
            LogUtil.debug(TAG, "Stopping notification thread");
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
                    LogUtil.debug(TAG, "Interrupted while waiting for a new " +
                            "item for notification -- likely shutting down");
                    return;
                } finally {
                    mNotificationsLock.unlock();
                }

                Event event;
                LogUtil.debug(TAG, "Received event number: " + mNotifications.size());
                while((event = mNotifications.poll()) != null) {
                    propagateEvent(event);
                }
            }
            LogUtil.debug(TAG, "Stopped event receive");
        }
    }
}
