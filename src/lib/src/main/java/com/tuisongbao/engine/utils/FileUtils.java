package com.tuisongbao.engine.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class FileUtils {
    private static final String TAG = "TSB" + FileUtils.class.getSimpleName();

    public static boolean isFileExists(String filePath) {
        if (StrUtils.isEmpty(filePath)) {
            return false;
        }
        File fileTest = new File(filePath);
        if (!fileTest.exists()) {
            LogUtils.warn(TAG, "Local file at " + filePath + " is no longer exists, need to download again");
            return false;
        }
        return true;
    }

    public static File getOutputFile(String filePath) {
        String fileFullPath;
        if (hasSDCard()) {
            fileFullPath = getSDCardPath() + filePath;
        } else {
            int lastSlashIndex = filePath.lastIndexOf("/");
            String fileName;
            if (lastSlashIndex < 0) {
                fileName = filePath;
            } else {
                fileName = filePath.substring(lastSlashIndex + 1);
            }
            fileFullPath = Environment.getDataDirectory() + fileName;
        }
        File file = new File(fileFullPath);
        if (file.exists()) {
            return file;
        }

        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
            return file;
        } catch (IOException e) {
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
