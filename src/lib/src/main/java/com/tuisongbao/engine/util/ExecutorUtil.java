package com.tuisongbao.engine.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class ExecutorUtil {
    private static final String TAG = "TSB" + ExecutorUtil.class.getSimpleName();
    private static ExecutorService mQueue;
    private static ScheduledExecutorService mTimers;

    private ExecutorUtil () {
        // empty
    }

    public synchronized static ExecutorService getThreadQueue() {
        if (mQueue == null) {
            mQueue = Executors.newSingleThreadExecutor(new DaemonThreadFactory("threadQueue"));
        }
        return mQueue;
    }

    public synchronized static ScheduledExecutorService getTimers() {
        if (mTimers == null) {
            mTimers = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("timers"));
        }
        return mTimers;
    }

    public synchronized static void shutdownThreads() {
        if (mQueue != null) {
            mQueue.shutdown();
            mQueue = null;
        }
        if (mTimers != null) {
            mTimers.shutdown();
            mTimers = null;
        }
    }

    private static class DaemonThreadFactory implements ThreadFactory {
        private final String mName;

        public DaemonThreadFactory(final String name) {
            this.mName = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName(TAG + ":" + mName);
            return t;
        }
    }
}
