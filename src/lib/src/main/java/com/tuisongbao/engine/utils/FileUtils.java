package com.tuisongbao.engine.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class FileUtils {
    private static final String TAG = "TSB" + FileUtils.class.getSimpleName();

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
