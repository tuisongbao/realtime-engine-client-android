package com.tuisongbao.android.engine.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import com.tuisongbao.android.engine.log.LogUtil;

public class DeviceUtil {

    private DeviceUtil() {
        // empty
    }

    public int getAppCode(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);

            return packageInfo.versionCode;
        } catch (Exception e) {
            return 0;
        }
    }

    public String getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
            return "version 1.0";
        }
    }

    public ApplicationInfo getAppInfo(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), 0);

        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }

        return null;
    }

    public String getAppName(Context context) {
        try {
            ApplicationInfo appInfo = this.getAppInfo(context);
            if (appInfo != null && null != context)
                return context.getPackageManager().getApplicationLabel(appInfo)
                        .toString();
        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }

        return null;
    }

    public static String getSystemProperty(String propName) {
        String result = "";
        try {
            Process process = Runtime.getRuntime().exec("getprop " + propName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()), 1024);
            result = reader.readLine();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getDeviceModel() {
        return android.os.Build.MODEL;
    }
}
