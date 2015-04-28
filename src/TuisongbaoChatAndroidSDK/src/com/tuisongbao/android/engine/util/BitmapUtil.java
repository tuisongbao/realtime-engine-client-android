package com.tuisongbao.android.engine.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.log.LogUtil;

public class BitmapUtil {

    /***
     * Download image and save to local, return path of the local image file.
     *
     * @param callback
     */
    public static void downloadImageIntoLocal(final String urlString, final String fileName, final TSBEngineCallback<String> callback) {
        LogUtil.info(LogUtil.LOG_TAG_CHAT, "Begine to download resource from " + urlString + " and save it into local fileName " + fileName);
        ExecutorUtil.getThreadQueue().execute(new Runnable() {

            @Override
            public void run() {
                URL url;
                try {
                    url = new URL(urlString);
                    Bitmap bitmap = BitmapFactory.decodeStream(url.openStream());
                    String filePath = saveImage(bitmap, fileName);
                    callback.onSuccess(filePath);
                } catch (Exception e) {
                    LogUtil.error(LogUtil.LOG_TAG_CHAT, e);
                    callback.onError(EngineConstants.ENGINE_CODE_UNKNOWN, "Download image failed, check log to find reason");
                }
            }
        });
    }

    public static String saveImage(Bitmap bmp, String fileName) throws IOException {
        File file;
        String filePath = null;
        if (hasSDCard()) {
            filePath = getSDCardPath() + "/tuisongbao/images/" + fileName + ".png";

        } else {
            filePath = Environment.getDataDirectory() + fileName;
        }
        file = new File(filePath);
        file.getParentFile().mkdirs();
        file.createNewFile();

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
