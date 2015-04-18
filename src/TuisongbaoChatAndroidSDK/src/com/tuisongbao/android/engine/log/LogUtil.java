package com.tuisongbao.android.engine.log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

import com.tuisongbao.android.engine.task.BaseAsyncTask;
import com.tuisongbao.android.engine.task.HttpURLConnectionCallBack;
import com.tuisongbao.android.engine.util.StrUtil;

public class LogUtil {
    public static final String LOG_TAG = "com.tuisongbao.android.push";
    public static final String LOG_TAG_SERVICE = "com.tuisongbao.android.engine.service";

    // network related tags
    public static final String LOG_TAG_CHAT = "com.tuisongbao.android.engine.chat";
    public static final String LOG_TAG_CHAT_CACHE = "com.tuisongbao.android.engine.chat.cache";
    public static final String LOG_TAG_TSB_ENGINE = "com.tuisongbao.android.engine.TSBEngine";
    public static final String LOG_TAG_CHANNEL = "com.tuisongbao.android.engine.channel";
    public static final String LOG_TAG_SQLITE = "com.tuisongbao.android.engine.sqlite";
    public static final String LOG_TAG_ENGINEIO = "com.tuisongbao.android.engineio";
    public static final String LOG_TAG_HTTP = "com.tuisongbao.android.engine.http";
    public static final String LOG_TAG_DEBUG_MSG = "com.tuisongbao.android.notification.debug";
    public static final String LOG_TAG_UNCAUGHT_EX = "com.tuisongbao.android.engine.unhandled";
    public static final String LOG_FILE_NAME_STRING = "push-error-logs.txt";


    public static int mLogLevel = 0;
    public static boolean mLogSwitch = true;
    public static long lastSendLogTime = -1;

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

    public static void sendErrorLogToServer() {
        LogUtil.debug(LogUtil.LOG_TAG_DEBUG_MSG,
                "Test the time is right to send log.");
        if (true) {
            return;
        }
        long currentTime = new Date().getTime();
        // only to meet this condition, before send error log.
        LogUtil.debug(LogUtil.LOG_TAG_DEBUG_MSG,
                "Test the time is right to send log.");

        if (lastSendLogTime < 0
                || currentTime - lastSendLogTime > 24 * 60 * 60 * 1000) {

            lastSendLogTime = currentTime;
            LogUtil.debug(LogUtil.LOG_TAG_DEBUG_MSG,
                    "Begin to executeSendErrorLog");

            executeSendErrorLog();

        } else {
            LogUtil.warn(LOG_TAG_DEBUG_MSG, "Have just send the error log!");
        }
    }

    private static void executeSendErrorLog() {
        try {

            File file = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    LogUtil.LOG_FILE_NAME_STRING);

            if (!file.exists()) {
                LogUtil.warn(LOG_TAG, "The log file is not exitst!");
                return;
            }

//            executeHttpConnection(file, new HttpURLConnectionCallBack() {
//                @Override
//                public void done(String result, PushException e) {
//
//                    try {
//                        result.trim();
//                        if ((result.startsWith("{") && result.endsWith("}"))
//                                || (result.startsWith("[") && result
//                                        .endsWith("]"))) {
//
//                            JSONObject json = new JSONObject(result);
//                        }
//                    } catch (Exception e2) {
//                        LogUtil.error(LogUtil.LOG_TAG, e2);
//                    }
//
//                }
//            });

        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG, e);
        }
    }

    @SuppressWarnings("rawtypes")
    private static synchronized void executeHttpConnection(final File file,
            HttpURLConnectionCallBack callback) throws Exception {

        @SuppressWarnings("unchecked")
        BaseAsyncTask<Void> sendErrorLogTask = new BaseAsyncTask(callback) {

            @Override
            public String run() {
                BufferedReader reader = null;
                String result = "";
                try {

                    final String BOUNDARY = "---------7d4a6d158c9";
                    final String RETURN = "\r\n";
                    final String PREFIX = "--" + BOUNDARY;
                    URL url = new URL("");
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();

                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Charsert", "UTF-8");

                    conn.setRequestProperty("Content-Type",
                            "multipart/form-data; boundary=" + BOUNDARY);

                    String fileContentString = getAllContentOfFile(file);
                    TreeMap<String, String> params = getTreeMapByArrays();
//                    params.put(HttpParams.fileMD5,
//                            RegisterManager.Md5(fileContentString));

                    StringBuilder sb = new StringBuilder();
                    sb.append(PREFIX + RETURN);
                    sb.append("Content-Disposition: form-data;name=\"sign\""
                            + RETURN);
                    sb.append(RETURN);

                    Iterator<String> it = params.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        sb.append(PREFIX + RETURN);
                        sb.append("Content-Disposition: form-data;name=\""
                                + key + "\"" + RETURN);
                        sb.append(RETURN);
                        sb.append(params.get(key) + RETURN);
                    }

                    sb.append(PREFIX + RETURN);
                    sb.append("Content-Disposition: form-data;name=\"file\";filename=\""
                            + file.getName() + "\"" + RETURN);

                    sb.append("Content-Type: test/plain" + RETURN);
                    sb.append(RETURN);
                    sb.append(fileContentString + RETURN);
                    sb.append(PREFIX + "--");

                    OutputStream out = new DataOutputStream(
                            conn.getOutputStream());
                    out.write(sb.toString().getBytes());

                    out.flush();
                    out.close();

                    reader = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    result = reader.readLine();
                    reader.close();

                } catch (Exception e) {
                    error(LOG_TAG, e);
                }
                return result;

            }
        };
//        BaseAsyncTask.executeTask(sendErrorLogTask);
    }

    @SuppressLint("SimpleDateFormat")
    private static void writeError(String tag, String msg, Throwable e) {

        try {
            boolean mExternalStorageAvailable = false;
            boolean mExternalStorageWriteable = false;
            String state = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                // We can read and write the media
                mExternalStorageAvailable = mExternalStorageWriteable = true;
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                // We can only read the media
                mExternalStorageAvailable = true;
                mExternalStorageWriteable = false;
            } else {
                // Something else is wrong. It may be one of many other states,
                // but all we need
                // to know is we can neither read nor write
                mExternalStorageAvailable = mExternalStorageWriteable = false;
            }

            if (mExternalStorageWriteable && mExternalStorageAvailable) {
                String stackTrace = "";
                String error = "";

                if (!StrUtil.isEmpty(msg)) {
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
                            Environment
                                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            LogUtil.LOG_FILE_NAME_STRING);

                    // ensure that the log file is less than 1M.
                    if (file.length() < 1 * 1024 * 1024) {
                        writer = new FileWriter(file, true);
                        writer.append(errorMsg);
                    }

                } catch (Throwable t) {
                    Log.i(LOG_TAG_UNCAUGHT_EX, "Throwable" + t.getMessage());

                } finally {
                    if (writer != null)
                        writer.close();
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private static boolean deleteLogFile() {
        try {
            File file = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    LogUtil.LOG_FILE_NAME_STRING);
            return file.delete();
        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG, e);
        }
        return false;
    }

    private static String getAllContentOfFile(File file) {
        String content = "";
        String s = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            s = br.readLine();
            while (s != null) {
                content += s;
                s = br.readLine();
            }
            br.close();

        } catch (Exception e) {
            LogUtil.error(LOG_TAG, e);
        }

        return content;
    }

    private static TreeMap<String, String> getTreeMapByArrays() {
        TreeMap<String, String> result = new TreeMap<String, String>();

//        result.put(HttpParams.apiKey, RegisterManager.getApiKey());
//        result.put(HttpParams.appKey, PushConfig.instance().getAppId());
//        result.put(HttpParams.token, XgcmPreference.instance().getToken());
//        result.put(HttpParams.deviceName, HttpParams.deviceNameDefaultValue);
//        result.put("deviceType", "Android");
//        result.put("deviceOS", "Android");
//        result.put(HttpParams.fileName, LogUtil.LOG_FILE_NAME_STRING);

        return result;
    }
}