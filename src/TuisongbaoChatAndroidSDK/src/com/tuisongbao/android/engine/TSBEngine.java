package com.tuisongbao.android.engine;

import android.content.Context;

import com.tuisongbao.android.engine.common.ITSBResponseMessage;
import com.tuisongbao.android.engine.connection.TSBConnectionManager;
import com.tuisongbao.android.engine.engineio.DataPipeline;
import com.tuisongbao.android.engine.engineio.EngineManager;
import com.tuisongbao.android.engine.engineio.sink.TSBListenerSink;
import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.service.RawMessage;
import com.tuisongbao.android.engine.util.StrUtil;

public final class TSBEngine {

    public static TSBConnectionManager connection = TSBConnectionManager.getInstance();
    private static Context mApplicationContext = null;
    private static DataPipeline mDataPipeline = new DataPipeline();
    private static TSBListenerSink mNotifier = new TSBListenerSink();
    private static String mSocketId;
    private static EngineManager mEngineManger = EngineManager.getInstance();
    private static Long mRequestId = 1L;

    private TSBEngine() {
        // empty here
    }

    /**
     * Initialize engine and start engine service.
     * 
     * @param context application conetext
     * @param appId tuisong bao app id
     * @param authEndpoint auth endpoint 
     */
    public static void init(Context context, String appId, String authEndpoint) {

        // save the application context
        mApplicationContext = context.getApplicationContext();
        try {
            boolean initialized = EngineConfig.instance().init(context, appId, authEndpoint);
            if (!initialized) {
                return;
            } else {
                LogUtil.info(LogUtil.LOG_TAG_PUSH_MANAGER,
                        "Successfully loaded configurations.");
            }
            // 初始化实时引擎
            initEngine();

        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }
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
        RawMessage message = new RawMessage(EngineConfig.instance()
                .getAppId(), EngineConfig.instance()
                .getAppKey(), name, data);
        message.setRequestId(getRequestId());
        if (response != null) {
            mNotifier.register(message, response);
        }
        return mEngineManger.send(message);
    }

    public static void bind(String bindName, ITSBResponseMessage response) {
        if (response != null && !StrUtil.isEmpty(bindName)) {
            RawMessage message = new RawMessage(EngineConfig.instance()
                    .getAppId(), EngineConfig.instance().getAppKey(),
                    bindName, null);
            message.setBindName(bindName);
            mNotifier.bind(bindName, response);
        } else {
            // empty
        }
        
    }

    public static void unbind(String bindName) {
        if (!StrUtil.isEmpty(bindName)) {
            RawMessage message = new RawMessage(EngineConfig.instance()
                    .getAppId(), EngineConfig.instance()
                    .getAppKey(), null, null);
            message.setBindName(bindName);
            mNotifier.unbind(bindName);
        } else {
            // empty
        }
        
    }

    public static void unbind(String bindName, ITSBResponseMessage response) {
        if (response != null && !StrUtil.isEmpty(bindName)) {
            RawMessage message = new RawMessage(EngineConfig.instance()
                    .getAppId(), EngineConfig.instance()
                    .getAppKey(), null, null);
            message.setBindName(bindName);
            mNotifier.unbind(bindName, response);
        } else {
            // empty
        }
        
    }

    private static void initEngine() {
        initializeDefaultSinks();
        mDataPipeline.addSource(EngineManager.getInstance().init(mApplicationContext));
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