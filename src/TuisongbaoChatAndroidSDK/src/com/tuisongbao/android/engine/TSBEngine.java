package com.tuisongbao.android.engine;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.tuisongbao.android.engine.common.ITSBResponseMessage;
import com.tuisongbao.android.engine.connection.TSBConnectionManager;
import com.tuisongbao.android.engine.engineio.DataPipeline;
import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.engineio.EngineManager;
import com.tuisongbao.android.engine.engineio.sink.TSBListenerSink;
import com.tuisongbao.android.engine.entity.TSBEngineConstants;
import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.service.EngineService;
import com.tuisongbao.android.engine.service.EngineServiceInterface;
import com.tuisongbao.android.engine.service.EngineServiceListener;
import com.tuisongbao.android.engine.service.RawMessage;
import com.tuisongbao.android.engine.util.StrUtil;

public final class TSBEngine {

    public static TSBConnectionManager connection = TSBConnectionManager.getInstance();
    private static Context mApplicationContext = null;
    private static EngineServiceInterface mService;
    private static DataPipeline mDataPipeline = new DataPipeline();
    private static TSBListenerSink mNotifier = new TSBListenerSink();
    private static String mSocketId;
    private static EngineManager mEngineManger = EngineManager.getInstance();
    private static Long mRequestId = 1L;

    private TSBEngine() {
        // empty here
    }

    /**
     * Open API for Push SDK integration.
     * 
     * @param context
     */
    public static void init(Context context, String appId, String appKey) {

        // save the application context
        mApplicationContext = context.getApplicationContext();
        EnginePreference.instance().init(context);
        try {
            boolean initialized = EngineConfig.instance().init(context, appId, appKey);
            if (!initialized) {
                return;
            } else {
                LogUtil.info(LogUtil.LOG_TAG_PUSH_MANAGER,
                        "Successfully loaded configurations.");
            }
            // 初始化实时引擎
            initEngine();
//            startEngineService(context);

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
     * 发送消息, 需要添加签名
     * 
     * @param message
     * @return
     */
    public static boolean send(String name, String sign, String data,
            ITSBResponseMessage response) {
        RawMessage message = new RawMessage(EngineConfig.instance()
                .getAppId(), EngineConfig.instance()
                .getAppKey(), name, data);
        message.setSignStr(sign);
        if (response != null) {
            mNotifier.register(message, response);
        }
        message.setRequestId(getRequestId());
        return mEngineManger.send(message);
//        if (mService != null) {
//            try {
//                RawMessage message = new RawMessage(EngineConfig.instance()
//                        .getAppId(), EngineConfig.instance()
//                        .getAppKey(), name, data);
//                message.setSignStr(sign);
//                if (response != null) {
//                    mNotifier.register(message, response);
//                    return mService.send(message, mEngineServiceListener);
//                } else {
//                    return mService.send(message, null);
//                }
//            } catch (RemoteException e) {
//                e.printStackTrace();
//                return false;
//            }
//        } else {
//            return false;
//        }
    }

    /**
     * 发送消息
     * 
     * @param message
     * @return
     */
    public static boolean send(String name, String data,
            ITSBResponseMessage response) {
        return send(name, null, data, response);
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
//        if (mService != null && response != null && !StrUtil.isEmpty(bindName)) {
//            try {
//                RawMessage message = new RawMessage(EngineConfig.instance()
//                        .getAppId(), EngineConfig.instance()
//                        .getAppKey(), bindName, null);
//                message.setBindName(bindName);
//                mNotifier.bind(bindName, response);
//                mService.bind(message, mEngineServiceListener);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        } else {
//            // empty
//        }
        
    }

    public static void unbind(String bindName) {
        if (StrUtil.isEmpty(bindName)) {
            RawMessage message = new RawMessage(EngineConfig.instance()
                    .getAppId(), EngineConfig.instance()
                    .getAppKey(), null, null);
            message.setBindName(bindName);
        } else {
            // empty
        }
//        if (mService != null) {
//            try {
//                RawMessage message = new RawMessage(EngineConfig.instance()
//                        .getAppId(), EngineConfig.instance()
//                        .getAppKey(), null, null);
//                message.setBindName(bindName);
//                mService.unbind(message, null);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//                // empty
//            }
//        } else {
//            // empty
//        }
        
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

    private static void startEngineService(Context context) {
        startEngineService(context, null);
    }

    private static void startEngineService(Context context,
            String removedPackageName) {
        try {

            Intent i = new Intent(mApplicationContext, EngineService.class);
            // context.startService(i);
            context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);

        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_PUSH_MANAGER, e);
        }
    }

    private static void initializeDefaultSinks() {
        mDataPipeline.addSink(mNotifier);
    }

    private static ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            LogUtil.debug(LogUtil.LOG_TAG_PUSH_MANAGER, "EngineService success");
            mService = EngineServiceInterface.Stub.asInterface(service);
            try {
                // bind connection change status
                bindConnectionChangeStatus();
                // bind socket id event
                mService.addEngineInterface(EngineConfig.instance().getAppId(), EngineConfig.instance().getAppKey());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            initializeDefaultSinks();
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            LogUtil.debug(LogUtil.LOG_TAG_PUSH_MANAGER,
                    "EngineService disconnected unexpectedly");
            mService = null;
        }
    };
    
    private static void bindConnectionChangeStatus() throws RemoteException {
        RawMessage message = new RawMessage(EngineConfig.instance()
                        .getAppId(), EngineConfig.instance()
                        .getAppKey(), null, null);
        message.setBindName(EngineConstants.EVENT_CONNECTION_CHANGE_STATUS);
        mService.bind(message, mConnectionEngineServiceListener);
    }

    /**
     * Connection listener
     */
    private static EngineServiceListener mConnectionEngineServiceListener = new EngineServiceListener.Stub() {

        @Override
        public void call(RawMessage value) throws RemoteException {
            if (value != null) {
                // 获取socket id
                if (EngineConstants.CONNECTION_NAME_SOCKET_ID.equals(value.getName())) {
                    mSocketId = value.getData();
                }
                value.setBindName(TSBEngineConstants.TSBENGINE_EVENT_CONNECTION_STATUS);
                mDataPipeline.receive(value);
            }
        }
    };

    private static EngineServiceListener mEngineServiceListener = new EngineServiceListener.Stub() {

        @Override
        public void call(RawMessage value) throws RemoteException {
            mDataPipeline.receive(value);
        }
    };
}