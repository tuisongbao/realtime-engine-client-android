package com.tuisongbao.android.engine.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.log.LogUtil;

public class DownloadUtil {

    /***
     * Download image and save to local, return path of the local image file.
     *
     * @param callback
     */
    public static void downloadResourceIntoLocal(final String urlString, final String outputFileName, final String resourceType, final TSBEngineCallback<String> callback) {
        LogUtil.info(LogUtil.LOG_TAG_CHAT, "Begine to download " + resourceType + " from " + urlString + " and save it into local fileName " + outputFileName);
        if (StrUtil.isEmpty(urlString)) {
            callback.onError(EngineConstants.ENGINE_CODE_INVALID_OPERATION, "The resource url String is invalid.");
            return;
        }
        ExecutorUtil.getThreadQueue().execute(new Runnable() {

            @Override
            public void run() {
                if (StrUtil.isEqual(resourceType, "image")) {
                    downloadImage(urlString, outputFileName + ".png", callback);
                } else if (StrUtil.isEqual(resourceType, "voice")) {
                    downloadVoice(urlString, outputFileName + ".wav", callback);
                }
            }
        });
    }

    public static void downloadVoice(final String urlString, final String outputFileName, final TSBEngineCallback<String> callback) {
        try {
            File outputFile = getOutputFile(outputFileName, "voices");
            String filePath = outputFile.getPath();

            URL url = new URL(urlString);
            InputStream inputStream = url.openStream();
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            int c;
            while ((c = inputStream.read()) != -1) {
                fileOutputStream.write(c);
            }
            fileOutputStream.close();

            callback.onSuccess(filePath);
        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_CHAT, e);
            callback.onError(EngineConstants.ENGINE_CODE_UNKNOWN, "Download voice failed, check log to find reason");
        }
    }

    public static void downloadImage(String urlString, String outputFileName, TSBEngineCallback<String> callback) {
        URL url;
        try {
            url = new URL(urlString);
            Bitmap bitmap = BitmapFactory.decodeStream(url.openStream());
            String filePath = saveImage(bitmap, outputFileName);
            callback.onSuccess(filePath);
        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_CHAT, e);
            callback.onError(EngineConstants.ENGINE_CODE_UNKNOWN, "Download image failed, check log to find reason");
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

    public static String saveImage(Bitmap bmp, String outputFileName) throws IOException {
        File outputFile = getOutputFile(outputFileName, "images");
        String filePath = outputFile.getPath();

        FileOutputStream out = new FileOutputStream(filePath);
        bmp.compress(CompressFormat.PNG, 100, out);
        out.flush();
        out.close();

        return filePath;
    }

    public static Bitmap loadFromFile(String filename) {
        try {
            File f = new File(filename);
            if (!f.exists()) { return null; }
            Bitmap tmp = BitmapFactory.decodeFile(filename);
            return tmp;
        } catch (Exception e) {
            return null;
        }
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
