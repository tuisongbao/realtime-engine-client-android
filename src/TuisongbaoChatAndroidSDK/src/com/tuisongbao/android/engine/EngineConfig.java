package com.tuisongbao.android.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class EngineConfig {

    public enum ServiceType
    {
        gcm, tps, mipush, hybrid;
    }

    // for gcm config
    private String GCM_PROJECT_ID;

    // for mipush config
    private String MIPUSH_APP_ID;
    private String MIPUSH_APP_KEY;

    // for tps config
    private String PUSH_APP_ID;
    private String PUSH_APP_KEY;
    private String SDK_SECRET;
    private boolean LOCATION_TRACK = true;
    private String PUSH_MSG_PROCESSOR = "com.tuisongbao.android.service.NotificationIntentService"; // default
    public String DEFAULT_MSG_PROCESSOR = "com.tuisongbao.android.service.NotificationIntentService";

    // for server url config
    private String PUSH_SERVER_URL = "https://api.tuisongbao.com";
    private String PUSH_SERVICE_ACTION = "com.tuisongbao.android.push.PushService";

    @SuppressWarnings("unused")
    private boolean DEBUG_SWITCH = false;

    private static EngineConfig mInstance = null;

    private ServiceType mServiceType = ServiceType.tps;

    private EngineConfig() {
        //empty here
    }

    public synchronized static EngineConfig instance() {
        if (null == mInstance) {
            mInstance = new EngineConfig();
        }

        return mInstance;
    }

    public String getServiceAction() {
        return PUSH_SERVICE_ACTION;
    }

    public boolean init(Context context, String appId) {

//        if (loadFromProperties(context, "pushconfig.properties")) {
//            if(isMiPushServiceAvailable()) {
//                mServiceType = ServiceType.mipush;
//            } else {
//                mServiceType = ServiceType.tps;
//            }
//            return true;
//        } else {
//            return false;
//        }
        PUSH_APP_ID = appId;
        return true;
    }

    public boolean isXiaoMiDevice() {
        return !StrUtil.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
    }

    public boolean isMiPushServiceAvailable() {
        return !StrUtil.isEmpty(MIPUSH_APP_ID) && !StrUtil.isEmpty(MIPUSH_APP_KEY) && isXiaoMiDevice();
    }

    public String getGCMProjectID()
    {
        return GCM_PROJECT_ID;
    }

    public String getMiPushAppId() {
        return MIPUSH_APP_ID;
    }

    public String getMiPushAppKey() {
        return MIPUSH_APP_KEY;
    }

    public String getAppIntentServicePath()
    {
        return PUSH_MSG_PROCESSOR;
    }

    public String getAppId()
    {
        return PUSH_APP_ID;
    }

    public String getAppKey()
    {
        if (StrUtil.isEmpty(PUSH_APP_KEY)) {
            return PUSH_APP_ID;
        }
        return PUSH_APP_KEY;
    }

    public String getSDKSecret() {
        return SDK_SECRET;
    }

    public boolean getLocationEnable() {
        return LOCATION_TRACK;
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

    public void setAppVersion(String newVersion) {
        EnginePreference.instance().setAppVersion(newVersion);
    }

    public String getAppVersion(Context context) {
        try
        {
            String appVersion = EnginePreference.instance().getAppVersion();
            if (!StrUtil.isEmpty(appVersion)) {
                return appVersion;
            }
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

    public String getNotificationIntentServicePath()
    {
        if (StrUtil.isEmpty(PUSH_MSG_PROCESSOR)) {
            return DEFAULT_MSG_PROCESSOR;
        }
        return PUSH_MSG_PROCESSOR;
    }

    public void setServiceType(ServiceType type) {
        mServiceType = type;
    }

    public ServiceType getServiceType()
    {
        return mServiceType;
    }

    public String getServiceString() {
        switch (mServiceType) {
            case mipush:
                return "mipush";
        default:
            return "tps";
        }
    }

    public boolean isMiPushService() {
        return mServiceType == ServiceType.mipush;
    }

    public boolean isTPSService() {
        return mServiceType == ServiceType.tps;
    }

    private boolean loadFromProperties(Context context, String properiesPath)
    {
        Resources localResources = context.getResources();
        AssetManager localAssetManager = localResources.getAssets();
        try
        {
            if (!Arrays.asList(localAssetManager.list("")).contains(properiesPath))
            {
                LogUtil.verbose(LogUtil.LOG_TAG, "Options - Couldn't find " + properiesPath);
                return false;
            }
        }
        catch (IOException localIOException1)
        {
            LogUtil.error(LogUtil.LOG_TAG, "passed in param properiesPath in loadFromProperties() is: " + properiesPath);
            LogUtil.error(LogUtil.LOG_TAG, localIOException1);
            return false;
        }

        Properties localProperties = new Properties();
        InputStream localInputStream = null;
        try
        {
            localInputStream = localAssetManager.open(properiesPath);
            localProperties.load(localInputStream);

            Class<? extends EngineConfig> localClass = getClass();
            List<Field> localList = Arrays.asList(localClass.getDeclaredFields());
            ListIterator<Field> localListIterator = localList.listIterator();
            while (localListIterator.hasNext())
            {
                Field localField = localListIterator.next();
                if (!EngineConfig.class.isAssignableFrom(localField.getType()))
                {
                    String str = localProperties.getProperty(localField.getName());
                    if (str != null)
                        try
                        {
                            if ((localField.getType() == Boolean.TYPE) || (localField.getType() == Boolean.class))
                            {
                                localField.set(this, Boolean.valueOf(str));
                                if (localField.getName().equals("DEBUG_SWITCH"))
                                {
                                    LogUtil.mLogSwitch = Boolean.valueOf(str);
                                }
                            }
                            else if ((localField.getType() == Long.TYPE) || (localField.getType() == Long.class)) localField.set(this,
                                    Long.valueOf(str));
                            else try
                            {
                                localField.set(this, str.trim());
                            }
                            catch (IllegalArgumentException localIllegalArgumentException)
                            {
                                LogUtil.error(LogUtil.LOG_TAG, "Unable to set field '" + localField.getName()
                                        + "' due to type mismatch.");

                                return false;
                            }
                        }
                        catch (IllegalAccessException localIllegalAccessException)
                        {
                            LogUtil.error(LogUtil.LOG_TAG, "Unable to set field '" + localField.getName()
                                    + "' because the field is not visible.");

                            return false;
                        }
                }
            }
        }
        catch (IOException localIOException2)
        {
            LogUtil.error(LogUtil.LOG_TAG, "passed in param properiesPath in loadFromProperties() is: " + properiesPath);
            LogUtil.error(LogUtil.LOG_TAG, localIOException2);

            return false;
        }

        return true;
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

    public String getServerUrl() {
        return PUSH_SERVER_URL;
    }
}
