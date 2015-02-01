package com.tuisongbao.android.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.tuisongbao.android.engine.common.ITSBResponseMessage;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.connection.TSBConnectionManager;
import com.tuisongbao.android.engine.connection.entity.TSBConnection;
import com.tuisongbao.android.engine.engineio.DataPipeline;
import com.tuisongbao.android.engine.engineio.EngineIoOptions;
import com.tuisongbao.android.engine.engineio.EngineManager;
import com.tuisongbao.android.engine.engineio.sink.TSBListenerSink;
import com.tuisongbao.android.engine.entity.TSBEngineConstants;
import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.service.RawMessage;
import com.tuisongbao.android.engine.util.DeviceUtil;
import com.tuisongbao.android.engine.util.ExecutorUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public final class TSBEngine {

    public static TSBConnectionManager connection = TSBConnectionManager.getInstance();
    private static Context mApplicationContext = null;
    private static DataPipeline mDataPipeline = new DataPipeline();
    private static TSBListenerSink mNotifier = new TSBListenerSink();
    private static EngineManager mEngineManger = EngineManager.getInstance();
    private static Long mRequestId = 1L;
    private static TSBEngineOptions mTSBEngineOptions;
    private static String mPushAppId;
    private static String mPushToken;
    private static String mPushService;

    private TSBEngine() {
        // empty here
    }

    /**
     * Initialize engine and start engine service.
     * 
     * @param context application conetext
     * @param options 
     */
    public static void init(Context context, TSBEngineOptions options) {

        // save the application context
        mApplicationContext = context.getApplicationContext();
        try {
            if (options == null || StrUtil.isEmpty(options.getAppId())
                    || StrUtil.isEmpty(options.getAuthEndpoint())
                    || options.getChatIntentService() == null) {
                return;
            } else {
                LogUtil.info(LogUtil.LOG_TAG_PUSH_MANAGER,
                        "Successfully loaded configurations.");
            }
            mTSBEngineOptions = options;
            // 初始化实时引擎
            initEngine();

        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }
    }
    
    public static Context getContext() {
        return mApplicationContext;
    }
    
    public static TSBEngineOptions getTSBEngineOptions() {
        return mTSBEngineOptions;
    }

    /**
     * Checks whether engine is connected
     * 
     * @return
     */
    public static boolean isConnected () {
        return mEngineManger.isConnected();
    }

    /**
     * Returns Connection socket id
     * 
     * @return
     */
    public static String getSocketId() {
        return mEngineManger.getSocketId();
    }

    /**
     * Sends message to engine service.
     * 
     * @param message
     * @return
     */
    public static boolean send(String name, String data,
            ITSBResponseMessage response) {
        if (!isIntialized()) {
            return false;
        }
        RawMessage message = new RawMessage(mTSBEngineOptions.getAppId(), mTSBEngineOptions
                .getAppId(), name, data);
        message.setRequestId(getRequestId());
        if (response != null) {
            mNotifier.register(message, response);
        }
        return mEngineManger.send(message);
    }

    public static void bind(String bindName, ITSBResponseMessage response) {
        if (!isIntialized()) {
            return;
        }
        if (response != null && !StrUtil.isEmpty(bindName)) {
            RawMessage message = new RawMessage(mTSBEngineOptions.getAppId(),
                    mTSBEngineOptions.getAppId(), bindName, null);
            message.setBindName(bindName);
            mNotifier.bind(bindName, response);
        } else {
            // empty
        }
        
    }

    public static void unbind(String bindName) {
        if (!isIntialized()) {
            return;
        }
        if (!StrUtil.isEmpty(bindName)) {
            RawMessage message = new RawMessage(mTSBEngineOptions.getAppId(),
                    mTSBEngineOptions.getAppId(), null, null);
            message.setBindName(bindName);
            mNotifier.unbind(bindName);
        } else {
            // empty
        }
        
    }

    public static void unbind(String bindName, ITSBResponseMessage response) {
        if (!isIntialized()) {
            return;
        }
        if (response != null && !StrUtil.isEmpty(bindName)) {
            RawMessage message = new RawMessage(mTSBEngineOptions.getAppId(),
                    mTSBEngineOptions.getAppId(), null, null);
            message.setBindName(bindName);
            mNotifier.unbind(bindName, response);
        } else {
            // empty
        }
        
    }
    
    private static boolean isIntialized() {
        return mTSBEngineOptions != null;
    }

    private static void initEngine() {
        initializeDefaultSinks();
        // bind connection, it must be called after initializeDefaultSinks
        bindConnection();
        EngineIoOptions engineIoOption = new EngineIoOptions();
        engineIoOption.setAppId(mTSBEngineOptions.getAppId());
        engineIoOption.setPlatform("Android$" + DeviceUtil.getDeviceModel());
        mDataPipeline.addSource(EngineManager.getInstance().init(engineIoOption));
    }

    private static void bindConnection() {
        connection.bind(TSBEngineConstants.TSBENGINE_BIND_NAME_CONNECTION_CONNECTED, mConnectionCallback);
    }

    private static long getRequestId() {
        synchronized (mRequestId) {
            mRequestId++;
            return mRequestId;
        }
    }

    private static void initializeDefaultSinks() {
        mDataPipeline.addSink(mNotifier);
    }

    private static void uploadPusConfig() {
        if (loadPushConfig()) {
            if (isConnected() && !StrUtil.isEmpty(mPushAppId) && !StrUtil.isEmpty(mPushService) && !StrUtil.isEmpty(mPushToken)) {
                sendPushConfig();
            } else {
                // delay 30s and retry
                ExecutorUtil.getTimers().schedule(new Runnable() {
                    
                    @Override
                    public void run() {
                        loadPushConfig();
                        sendPushConfig();
                    }
                }, 1000 * 30, TimeUnit.MILLISECONDS);
            }
        } else {
            // empty, not integrate with push 
        }
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
                if (StrUtil.isEmpty(mPushAppId) && pushConfig != null) {
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
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean sendPushConfig() {
        if (isConnected() && !StrUtil.isEmpty(mPushAppId) && !StrUtil.isEmpty(mPushService) && !StrUtil.isEmpty(mPushToken)) {
            JSONObject data = new JSONObject();
            try {
                data.put("appId", mPushAppId);
                data.put("service", mPushService);
                data.put("token", mPushToken);
                RawMessage message = new RawMessage(mTSBEngineOptions.getAppId(), mTSBEngineOptions
                        .getAppId(), "engine_connection:bindPush", data.toString());
                message.setRequestId(getRequestId());
                return mEngineManger.send(message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        } else {
            return false;
        }
    }

    private static TSBEngineCallback<TSBConnection> mConnectionCallback = new TSBEngineCallback<TSBConnection>() {

        @Override
        public void onSuccess(TSBConnection t) {
            // when reconnection to upload push config
            uploadPusConfig();
        }

        @Override
        public void onError(int code, String message) {
            
        }
    };
}