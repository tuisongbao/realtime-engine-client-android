package com.tuisongbao.engine.download;

import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.utils.FileUtils;
import com.tuisongbao.engine.utils.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * <STRONG>下载任务</STRONG>
 */
public class DownloadTask implements Runnable {
    private static final String TAG = "TSB" + DownloadTask.class.getSimpleName();

    private Thread mRunningThread;
    private boolean isCanceled = false;
    private String downloadUrl;
    private String fileName;
    private EngineCallback<String> resultCallback;
    private ProgressCallback progressCallback;

    /**
     * 下载资源并以文件形式存储，会优先存储在 SD 上，没有 SD 卡时存储在 {@code DATA_DIRECTORY} 目录下。
     *
     * @param downloadUrl       下载地址
     * @param fileName          输出文件名称，包含路径，例如 “/demo/image/cat.png”
     * @param resultCallback    结果处理方法
     * @param progressCallback  进度处理方法
     */
    public DownloadTask(String downloadUrl, String fileName, EngineCallback<String> resultCallback, ProgressCallback progressCallback) {
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.resultCallback = resultCallback;
        this.progressCallback = progressCallback;
    }

    @Override
    public void run() {
        mRunningThread = Thread.currentThread();

        try {
            downloadFileWithProgress(downloadUrl, fileName, resultCallback, progressCallback);
        } catch (Exception e) {
            ResponseError error = new ResponseError();
            error.setMessage(e.getMessage());
            resultCallback.onError(error);
            LogUtils.error(TAG, e);
        }
    }

    /**
     * 获取当前任务的执行状态
     *
     * @return  {true} 表示正在执行，{false} 表示还未执行。
     */
    public boolean isRunning() {
        return mRunningThread.isAlive();
    }

    /**
     * 停止任务
     */
    public void cancel() {
        isCanceled = true;
        if (mRunningThread != null) {
            mRunningThread.interrupt();
        }
    }

    private void downloadFileWithProgress(final String urlString, final String fileName, final EngineCallback<String> callback
            , final ProgressCallback progressCallback) throws Exception {
        File outputFile = FileUtils.getOutputFile(fileName);
        if (outputFile == null) {
            ResponseError error = new ResponseError();
            error.setMessage("创建输出文件 " + fileName + " 失败");
            callback.onError(error);
            return;
        }

        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        connection.connect();
        int lengthOfFile = connection.getContentLength();
        InputStream is = url.openStream();
        FileOutputStream fos = new FileOutputStream(outputFile);

        byte data[] = new byte[1024];
        long total = 0;
        int progress = 0;
        int count;
        while (!isCanceled && (count = is.read(data)) != -1) {
            total += count;
            int progress_temp = (int) total * 100 / lengthOfFile;
            if (progress_temp % 10 == 0 && progress != progress_temp) {
                progress = progress_temp;
            }
            if (progressCallback != null) {
                progressCallback.progress(progress_temp);
            }
            fos.write(data, 0, count);
        }
        is.close();
        fos.close();

        String filePath = outputFile.getPath();
        callback.onSuccess(filePath);
    }
}
