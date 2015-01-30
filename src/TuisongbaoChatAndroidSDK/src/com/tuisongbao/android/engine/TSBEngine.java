package com.tuisongbao.android.engine;

import android.content.Context;

import com.tuisongbao.android.engine.common.ITSBResponseMessage;
import com.tuisongbao.android.engine.connection.TSBConnectionManager;
import com.tuisongbao.android.engine.engineio.DataPipeline;
import com.tuisongbao.android.engine.engineio.EngineIoOptions;
import com.tuisongbao.android.engine.engineio.EngineManager;
import com.tuisongbao.android.engine.engineio.sink.TSBListenerSink;
import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.service.RawMessage;
import com.tuisongbao.android.engine.util.DeviceUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public final class TSBEngine {

    public static TSBConnectionManager connection = TSBConnectionManager.getInstance();
    private static Context mApplicationContext = null;
    private static DataPipeline mDataPipeline = new DataPipeline();
    private static TSBListenerSink mNotifier = new TSBListenerSink();
    private static String mSocketId;
    private static EngineManager mEngineManger = EngineManager.getInstance();
    private static Long mRequestId = 1L;
    private static TSBEngineOptions mTSBEngineOptions;

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
        return mSocketId;
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
        EngineIoOptions engineIoOption = new EngineIoOptions();
        engineIoOption.setAppId(mTSBEngineOptions.getAppId());
        engineIoOption.setPlatform("Android$" + DeviceUtil.getDeviceModel());
        mDataPipeline.addSource(EngineManager.getInstance().init(mApplicationContext, engineIoOption));
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
}