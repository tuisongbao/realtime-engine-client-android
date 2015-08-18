package com.tuisongbao.engine.utils;

import android.os.Environment;

import com.tuisongbao.engine.chat.message.entity.ChatMessage.TYPE;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.log.LogUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

public class DownloadUtils {
    private static final String TAG = "TSB" + DownloadUtils.class.getSimpleName();
    /***
     * Download image and save to local, return path of the local image file.
     *
     * @param urlString         下载 url
     * @param type              文件将会被保存在该 type 名字的目录下
     * @param callback          结果处理方法
     * @param progressCallback  进度处理方法
     */
    public static void downloadResourceIntoLocal(final String urlString, final TYPE type, final EngineCallback<String> callback
            , final ProgressCallback progressCallback) {
        LogUtil.info(TAG, "Begin to download " + type.getName() + " from " + urlString);
        ExecutorUtils.getThreadQueue().execute(new Runnable() {

            @Override
            public void run() {
                try {
                    // The last string is timestamp, use it to be the file name
                    String outputFileName = StrUtils.getTimestampStringOnlyContainNumber(new Date());
                    String folder;
                    // TODO: the suffix seems not work, no matter what the real format is, the image or voice can show and play respectively. Check why.
                    folder = type.getName();
                    if (type == TYPE.IMAGE) {
                        outputFileName += ".png";
                    } else if (type == TYPE.VOICE) {
                        outputFileName += ".wav";
                    } else if (type == TYPE.VIDEO) {
                        outputFileName += ".mp4";
                    }
                    downloadFileWithProgress(urlString, outputFileName, folder, callback, progressCallback);
                } catch (Exception e) {
                    ResponseError error = new ResponseError();
                    error.setMessage(e.getMessage());
                    callback.onError(error);
                    LogUtil.error(TAG, e);
                }
            }
        });
    }

    private static void downloadFileWithProgress(final String urlString, final String outputFileName, final String folder, final EngineCallback<String> callback
            , final ProgressCallback progressCallback) {
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            connection.connect();
            int lengthOfFile = connection.getContentLength();
            InputStream is = url.openStream();
            File outputFile = getOutputFile(outputFileName, folder);
            FileOutputStream fos = new FileOutputStream(outputFile);
            byte data[] = new byte[1024];
            long total = 0;
            int progress = 0;
            int count;
            while ((count = is.read(data)) != -1) {
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
        } catch (Exception e) {
            LogUtil.error(TAG, e);
            ResponseError error = new ResponseError();
            error.setMessage("Downloading resource failed " + e.getMessage());
            callback.onError(error);
        }
    }

    public static File getOutputFile(String outputFileName, String folderName) throws IOException {
        File file;
        String filePath;
        if (hasSDCard()) {
            filePath = getSDCardPath() + "/tuisongbao/" + folderName + "/" + outputFileName;

        } else {
            filePath = Environment.getDataDirectory() + outputFileName;
        }
        file = new File(filePath);
        file.getParentFile().mkdirs();
        file.createNewFile();

        return file;
    }

    public static boolean hasSDCard() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    public static String getSDCardPath() {
        File path = Environment.getExternalStorageDirectory();
        return path.getAbsolutePath();
    }
}
