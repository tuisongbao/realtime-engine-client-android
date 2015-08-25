package com.tuisongbao.engine.download;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// TODO: 15-8-25 Merge with ExecutorUtils
public class DownloadManager {
    private static DownloadManager mInstance;
    private ThreadPoolExecutor mThreadPool;
    final BlockingQueue<Runnable> mWorkQueue = new LinkedBlockingQueue<>();

    static {
        mInstance = new DownloadManager();
    }

    private DownloadManager() {
        // Sets the amount of time an idle thread waits before terminating
        final int KEEP_ALIVE_TIME = 1;
        // Sets the Time Unit to seconds
        final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

        // Creates a thread pool manager
        mThreadPool = new ThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                NUMBER_OF_CORES,       // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mWorkQueue);
    }

    public static DownloadManager getInstance() {
        return mInstance;
    }

    public void start(DownloadTask task) {
        /*
         * "Executes" the tasks' download Runnable in order to download the image. If no
         * Threads are available in the thread pool, the Runnable waits in the queue.
         */
        mThreadPool.execute(task);
    }

    public void cancel(DownloadTask task) {
        // FIXME: 15-8-25 I do not know this can work or not, need test
        if (!task.isRunning()) {
            mWorkQueue.remove(task);
        }
        task.cancel();
    }
}
