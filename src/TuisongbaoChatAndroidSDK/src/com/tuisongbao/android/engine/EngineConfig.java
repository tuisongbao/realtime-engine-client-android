package com.tuisongbao.android.engine;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class EngineConfig {

    // for engin config
    private String ENGINE_APP_ID;
    private String ENGINE_APP_KEY;
    
    // Auth endpoint
    private String AUTH_ENDPOINT;

    private static EngineConfig mInstance = null;

    private EngineConfig() {
        //empty here
    }

    public synchronized static EngineConfig instance() {
        if (null == mInstance) {
            mInstance = new EngineConfig();
        }

        return mInstance;
    }

    public boolean init(Context context, String appId, String endpoint) {
        if (StrUtil.isEmpty(appId) || StrUtil.isEmpty(endpoint)) {
            return false;
        } else {
            ENGINE_APP_ID = appId;
            AUTH_ENDPOINT = endpoint;
            return true;
        }
    }

    public boolean isXiaoMiDevice() {
        return !StrUtil.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
    }

    public String getAuthEndpoint()
    {
        return AUTH_ENDPOINT;
    }

    public String getAppId()
    {
        return ENGINE_APP_ID;
    }

    public String getAppKey()
    {
        if (StrUtil.isEmpty(ENGINE_APP_KEY)) {
            return ENGINE_APP_ID;
        }
        return ENGINE_APP_KEY;
    }

    public int getAppCode(Context context) {
        try
        {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            return packageInfo.versionCode;
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    public String getAppVersion(Context context) {
        try
        {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        }
        catch (Exception e)
        {
        	LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
            return "version 1.0";
        }
    }

    public ApplicationInfo getAppInfo(Context context) {
        try {
            return context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);

        } catch (Exception e) {
        	LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }

        return null;
    }

    public String getAppName(Context context)
    {
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024);
            result = reader.readLine();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
