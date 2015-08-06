package com.tuisongbao.engine.utils;

import android.os.Environment;

import com.tuisongbao.engine.chat.message.entity.ChatMessage.TYPE;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.callback.TSBProgressCallback;
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
     * @param urlString download url
     * @param type according to different type, save file into different folder
     * @param callback
     */
    public static void downloadResourceIntoLocal(final String urlString, final TYPE type, final TSBEngineCallback<String> callback
            , final TSBProgressCallback progressCallback) {
        LogUtil.info(TAG, "Begin to download " + type.getName() + " from " + urlString);
        ExecutorUtils.getThreadQueue().execute(new Runnable() {

            @Override
            public void run() {
                try {
                    // The last string is timestamp, use it to be the file name
                    String outputFileName = StrUtils.getTimestampStringOnlyContainNumber(new Date());
                    String folder = "";
                    // TODO: the suffix seems not work, no matter what the real format is, the image or voice can show and play respectively. Check why.
                    if (type == TYPE.IMAGE) {
                        outputFileName += ".png";
                        folder = "images";
                    } else if (type == TYPE.VOICE) {
                        outputFileName += ".wav";
                        folder = "voices";
                    } else if (type == TYPE.VIDEO) {
                        outputFileName += ".mp4";
                        folder = "videos";
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

    private static void downloadFileWithProgress(final String urlString, final String outputFileName, final String folder, final TSBEngineCallback<String> callback
            , final TSBProgressCallback progressCallback) {
        try {
            URL url = new URL(urlString);
            URLConnection conexion = url.openConnection();
            conexion.connect();
            int lenghtOfFile = conexion.getContentLength();
            InputStream is = url.openStream();
            File outputFile = getOutputFile(outputFileName, folder);
            FileOutputStream fos = new FileOutputStream(outputFile);
            byte data[] = new byte[1024];
            long total = 0;
            int progress = 0;
            int count = 0;
            while ((count = is.read(data)) != -1) {
                total += count;
                int progress_temp = (int) total * 100 / lenghtOfFile;
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
        }
    }

    public static File getOutputFile(String outputFileName, String folderName) throws IOException {
        File file;
        String filePath = null;
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
