package com.tuisongbao.engine.log;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

import com.tuisongbao.engine.utils.StrUtils;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
    private static final String TAG = "TSB" + LogUtil.class.getSimpleName();
    private static final String LOG_FILE_NAME_STRING = "push-error-logs.txt";


    public static int mLogLevel = 0;
    public static boolean mLogSwitch = true;

    public static void warn(String tag, String msg) {
        if (mLogLevel <= 5 && mLogSwitch)
            Log.w(tag, msg);
    }

    public static void warn(String tag, String msg, Throwable paramThrowable) {
        if (mLogLevel <= 5 && mLogSwitch)
            Log.w(tag, msg, paramThrowable);
    }

    public static void warn(String tag, Throwable paramThrowable) {
        if (mLogLevel <= 5 && mLogSwitch)
            Log.w(tag, paramThrowable);
    }

    public static void verbose(String tag, String msg) {
        if (mLogLevel <= 2 && mLogSwitch)
            Log.v(tag, msg);
    }

    public static void debug(String tag, String msg) {
        if (mLogLevel <= 3 && mLogSwitch)
            Log.d(tag, msg);
    }

    public static void info(String tag, String msg) {
        if (mLogLevel <= 4 && mLogSwitch)
            Log.i(tag, msg);
    }

    public static void info(String tag, String msg, Throwable paramThrowable) {
        if (mLogLevel <= 4 && mLogSwitch)
            Log.i(tag, msg, paramThrowable);
    }

    public static void error(String tag, String msg) {
        if (mLogLevel <= 6 && mLogSwitch)
            Log.e(tag, msg);

        writeError(tag, msg, null);
    }

    public static void error(String tag, Throwable paramThrowable) {
        if (mLogLevel <= 6 && mLogSwitch)
            Log.e(tag, null, paramThrowable);

        writeError(tag, "", paramThrowable);
    }

    public static void error(String tag, String msg, Throwable paramThrowable) {
        if (mLogLevel <= 6 && mLogSwitch)
            Log.e(tag, msg, paramThrowable);

        writeError(tag, msg, paramThrowable);
    }

    @SuppressLint("SimpleDateFormat")
    private static void writeError(String tag, String msg, Throwable e) {

        try {
            boolean mExternalStorageAvailable = false;
            boolean mExternalStorageWritable = false;
            String state = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                // We can read and write the media
                mExternalStorageAvailable = mExternalStorageWritable = true;
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                // We can only read the media
                mExternalStorageAvailable = true;
                mExternalStorageWritable = false;
            } else {
                // Something else is wrong. It may be one of many other states,
                // but all we need
                // to know is we can neither read nor write
                mExternalStorageAvailable = mExternalStorageWritable = false;
            }

            if (mExternalStorageWritable && mExternalStorageAvailable) {
                String stackTrace = "";
                String error = "";

                if (!StrUtils.isEmpty(msg)) {
                    error = msg;
                }

                if (null != e) {
                    stackTrace = Log.getStackTraceString(e);
                    error = String.format("%s \r\n %s", error, stackTrace);
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "yyyy/MM/dd HH:mm:ss");
                // get current date time with Date()

                String errorMsg = String.format("%s %s %s",
                        dateFormat.format(new Date()), tag, error);

                File file = null;
                FileWriter writer = null;
                try {
                    file = new File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            LogUtil.LOG_FILE_NAME_STRING);

                    // ensure that the log file is less than 1M.
                    if (file.length() < 1 * 1024 * 1024) {
                        writer = new FileWriter(file, true);
                        writer.append(errorMsg);
                    }

                } catch (Throwable t) {
                    Log.i(TAG, "Throwable" + t.getMessage());

                } finally {
                    if (writer != null)
                        writer.close();
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
