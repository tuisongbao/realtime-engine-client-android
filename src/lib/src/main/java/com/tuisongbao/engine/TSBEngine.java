package com.tuisongbao.engine;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.tuisongbao.engine.channel.TSBChannelManager;
import com.tuisongbao.engine.chat.TSBChatManager;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.TSBEngineCallback;
import com.tuisongbao.engine.connection.AutoReconnectConnection;
import com.tuisongbao.engine.connection.Connection;
import com.tuisongbao.engine.connection.entity.TSBConnectionEvent;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.service.RawMessage;
import com.tuisongbao.engine.util.ExecutorUtil;
import com.tuisongbao.engine.util.StrUtil;

public final class TSBEngine {
    public static AutoReconnectConnection connection;
    public static TSBChatManager chatManager;
    public static TSBChannelManager channelManager;

    private static final String TAG = TSBEngine.class.getSimpleName();

    private static Context mApplicationContext = null;
    private TSBEngineOptions mEngineOptions;
    private String mPushAppId;
    private String mPushToken;
    private String mPushService;

    public TSBEngine(Context context, TSBEngineOptions options) {

        // save the application context
        mApplicationContext = context.getApplicationContext();
        try {
            mEngineOptions = options;
            if (options == null || StrUtil.isEmpty(mEngineOptions.getAppId())) {
                LogUtil.warn(LogUtil.LOG_TAG_TSB_ENGINE
                        , "No AppId, you do not have permission to use cool engine!");
                return;
            }

            connection = new AutoReconnectConnection(this);

            if (StrUtil.isEmpty(mEngineOptions.getAuthEndpoint())) {
                LogUtil.warn(LogUtil.LOG_TAG_TSB_ENGINE
                        , "No auth endpoint, you only can subscribe public channel, and can not implement cool Chat!");
                return;
            } else if (mEngineOptions.getChatIntentService() == null) {
                LogUtil.warn(LogUtil.LOG_TAG_TSB_ENGINE
                        , "No Intent service specified for receiving chat messages, " +
                            "you only can use Pub/Sub feature, if this is what you want, ignore this warning!");
                // TODO: Init ChannelManager
                channelManager = new TSBChannelManager(this);
            } else {
                LogUtil.info(LogUtil.LOG_TAG_TSB_ENGINE,
                        "Successfully load configurations for engine.");
                // TODO: Init ChatManager and ChannelManager
                chatManager = new TSBChatManager(this);
            }
            // TODO: When success connected, bind push if chat is enabled

        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
    }

    public static Context getContext() {
        return mApplicationContext;
    }

    public TSBEngineOptions getEngineOptions() {
        return mEngineOptions;
    }

    private void loadPushConfig() throws ClassNotFoundException, NoSuchFieldException
            , IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Class forName = Class.forName("com.tuisongbao.android.PushConfig");
        // get push app id
        if (forName != null) {
            // get push config instance
            Field pushConfigInstance = forName.getDeclaredField("mInstance");
            pushConfigInstance.setAccessible(true);
            Object pushConfig = pushConfigInstance.get(forName);
            if (StrUtil.isEmpty(mPushAppId) && pushConfig != null) {
                Field field = forName.getDeclaredField("PUSH_APP_ID");
                field.setAccessible(true);
                Object appId = field.get(pushConfig);
                if (appId != null && appId instanceof String) {
                    mPushAppId = (String)appId;
                }
            }
            // get push app id
            if (StrUtil.isEmpty(mPushService) && pushConfig != null) {
                Field field = forName.getDeclaredField("mServiceType");
                field.setAccessible(true);
                // service type
                Object serviceType = field.get(pushConfig);
                if (serviceType != null && serviceType instanceof Enum) {
                    mPushService = ((Enum)serviceType).name();
                }
            }
            // get token
            if (StrUtil.isEmpty(mPushToken)) {
                forName = Class.forName("com.tuisongbao.android.PushPreference");
                if (forName != null) {
                    // get push config instance
                    Field pushPreferenceInstance = forName.getDeclaredField("mInstance");
                    pushPreferenceInstance.setAccessible(true);
                    Object pushPreference = pushPreferenceInstance.get(forName);
                    if (pushPreference != null) {
                        Method method = forName.getMethod("getAppToken");
                        String token = (String)method.invoke(pushPreference);
                        if (!StrUtil.isEmpty(token)) {
                            mPushToken = token;
                        }
                    }
                }
            }
        }
    }

    private void sendPushEvent() throws JSONException {
        if (!StrUtil.isEmpty(mPushAppId) && !StrUtil.isEmpty(mPushService) && !StrUtil.isEmpty(mPushToken)) {
            JSONObject data = new JSONObject();
            data.put("appId", mPushAppId);
            data.put("service", mPushService);
            data.put("token", mPushToken);
            // TODO: Send bindPush event
        }
    }
}
