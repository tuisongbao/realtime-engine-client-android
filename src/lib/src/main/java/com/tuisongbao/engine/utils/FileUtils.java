package com.tuisongbao.engine.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class FileUtils {
    private static final String TAG = "TSB" + FileUtils.class.getSimpleName();

    /**
     * 查看文件是否存在
     *
     * @param filePath 文件的绝对路径
     * @return true 表示存在， false 表示不存在
     */
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

    /**
     * 获取在 filePath 下的文件，如果已经存在，返回该文件，不存在，返回 {@code null}。
     * 优先在 SD 卡目录下找；没有 SD 卡，则在 {@link Environment#getDataDirectory()} 目录下找以 filename 命名的文件。
     *
     * @param filePath 必须有这样的结构 "/dir1/dir2/filename.txt"，以 "/" 开头
     * @return 指定位置的文件
     */
    public static File find(String filePath) {
        String fileFullPath;
        File file;
        if (hasSDCard()) {
            fileFullPath = getSDCardPath() + filePath;
            file = new File(fileFullPath);
            if (file.exists()) {
                return file;
            }
        } else {
            int lastSlashIndex = filePath.lastIndexOf("/");
            String fileName;
            if (lastSlashIndex < 0) {
                fileName = filePath;
            } else {
                fileName = filePath.substring(lastSlashIndex + 1);
            }
            fileFullPath = Environment.getDataDirectory() + fileName;
            file = new File(fileFullPath);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    /**
     * 获取在 filePath 下的文件，如果已经存在，返回该文件，不存在，创建一个新的。
     * 优先存储在 SD 卡中，没有 SD 卡，则放在 {@link Environment#getDataDirectory()} 目录下。
     *
     * @param filePath 必须有这样的结构 "/dir1/dir2/filename.txt"，以 "/" 开头
     * @return 指定位置的文件，如果创建文件失败，返回 {@code null}
     */
    public static File load(String filePath) {
        File file = find(filePath);
        if (file != null) {
            return file;
        }

        file = new File(getAvailableFullPath(filePath));
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
            return file;
        } catch (IOException e) {
            return null;
        }
    }

    public static String getAvailableFullPath(String filePath) {
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
        return fileFullPath;
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
