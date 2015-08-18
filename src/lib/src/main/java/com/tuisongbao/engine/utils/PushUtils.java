package com.tuisongbao.engine.utils;

import com.google.gson.JsonObject;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.common.entity.RawEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class PushUtils {
    private static final String TAG = PushUtils.class.getSimpleName();

    private static String mPushAppId;
    private static String mPushToken;
    private static String mPushService;

    public static void bindPush(Engine engine) {
        if (!loadPushConfig()) {
            return;
        }
        waitTokenAndSendBindEvent(engine);
    }

    private static void waitTokenAndSendBindEvent(final Engine engine) {
        if (engine.getConnection().isConnected()) {
            if (StrUtils.isEmpty(mPushToken)) {
                loadPushConfig();
            }

            // Now we get the push token
            if (!StrUtils.isEmpty(mPushToken)) {
                sendBindPushEvent(engine);
                return;
            }
            // We not get the push token, wait for a second and retry.
        }
        // If connection is not available or the push token is null, retry.
        LogUtils.info(TAG, "Waiting... now the connection is " + engine.getConnection().isConnected()
                + " and token " + mPushToken);
        ExecutorUtils.getTimers().schedule(new Runnable() {
            @Override
            public void run() {
                waitTokenAndSendBindEvent(engine);
            }
        }, 30, TimeUnit.SECONDS);
    }

    /**
     * Returns whether the app is integrate with push
     *
     * @return true if the app is integrate with push, or false
     */
    private static boolean loadPushConfig() {
        try {
            Class forName = Class.forName("com.tuisongbao.android.PushConfig");
            // get push app id
            if (forName != null) {
                // get push config instance
                Field pushConfigInstance = forName.getDeclaredField("mInstance");
                pushConfigInstance.setAccessible(true);
                Object pushConfig = pushConfigInstance.get(forName);
                if (StrUtils.isEmpty(mPushAppId) && pushConfig != null) {
                    Field field = forName.getDeclaredField("PUSH_APP_ID");
                    field.setAccessible(true);
                    Object appId = field.get(pushConfig);
                    if (appId != null && appId instanceof String) {
                        mPushAppId = (String)appId;
                    } else {
                        return true;
                    }
                }
                // get push app id
                if (StrUtils.isEmpty(mPushService) && pushConfig != null) {
                    Field field = forName.getDeclaredField("mServiceType");
                    field.setAccessible(true);
                    // service type
                    Object serviceType = field.get(pushConfig);
                    if (serviceType != null && serviceType instanceof Enum) {
                        mPushService = ((Enum)serviceType).name();
                    }
                }
                // get token
                if (StrUtils.isEmpty(mPushToken)) {
                    forName = Class.forName("com.tuisongbao.android.PushPreference");
                    if (forName != null) {
                        // get push config instance
                        Field pushPreferenceInstance = forName.getDeclaredField("mInstance");
                        pushPreferenceInstance.setAccessible(true);
                        Object pushPreference = pushPreferenceInstance.get(forName);
                        if (pushPreference != null) {
                            Method method = forName.getMethod("getAppToken");
                            String token = (String)method.invoke(pushPreference);
                            if (!StrUtils.isEmpty(token)) {
                                mPushToken = token;
                            }
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            LogUtils.warn(TAG, "Load push config failed", e);
        }
        return false;
    }

    private static void sendBindPushEvent(Engine engine) {
        RawEvent event = new RawEvent("engine_connection:bindPush");
        JsonObject data = new JsonObject();
        data.addProperty("appId", mPushAppId);
        data.addProperty("service", mPushService);
        data.addProperty("token", mPushToken);
        event.setData(data);

        engine.getConnection().send(event);
    }
}
