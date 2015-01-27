package com.tuisongbao.android.engine;

import java.util.concurrent.ConcurrentHashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.tuisongbao.android.engine.common.ITSBResponseMessage;
import com.tuisongbao.android.engine.common.TSBEngineBindCallback;
import com.tuisongbao.android.engine.engineio.DataPipeline;
import com.tuisongbao.android.engine.engineio.interfaces.IEngineInterface;
import com.tuisongbao.android.engine.engineio.sink.TSBListenerSink;
import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.service.EngineService;
import com.tuisongbao.android.engine.service.EngineServiceInterface;
import com.tuisongbao.android.engine.service.EngineServiceListener;
import com.tuisongbao.android.engine.service.RawMessage;

public final class TSBEngine {

    private static Context mApplicationContext = null;
    private static EngineServiceInterface mService;
    private static ConcurrentHashMap<String, TSBEngineListener> mTBSEngineListener = new ConcurrentHashMap<String, TSBEngine.TSBEngineListener>();
    private static String mAppId;
    private static DataPipeline mDataPipeline = new DataPipeline();
    private static ConcurrentHashMap<String, IEngineInterface> mEngineInterfaceMap = new ConcurrentHashMap<String, IEngineInterface>();
    private static TSBListenerSink mNotifier = new TSBListenerSink();

    private TSBEngine() {
        // empty here
    }

    /**
     * Open API for Push SDK integration.
     * 
     * @param context
     */
    public static void init(Context context, String appId) {

        mAppId = appId;
        mApplicationContext = context.getApplicationContext(); // save the
                                                               // application
                                                               // context
        EnginePreference.instance().init(context);
        try {
            boolean initialized = EngineConfig.instance().init(context, appId);
            if (!initialized) {
                return;
            } else {
                LogUtil.info(LogUtil.LOG_TAG_PUSH_MANAGER,
                        "Successfully loaded configurations.");
            }
            startEngineService(context);

        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }
    }

    /**
     * 发送消息
     * 
     * @param message
     * @return
     */
    public static boolean send(String name, String data,
            ITSBResponseMessage response) {
        if (mService != null) {
            try {
                RawMessage message = new RawMessage(EngineConfig.instance()
                        .getAppId(), name, data);
                mNotifier.register(message, response);
                return mService.send(message, mEngineServiceListener);
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean bind(String bindName, TSBEngineBindCallback callback) {
        if (mService != null) {
            try {
                RawMessage message = new RawMessage(EngineConfig.instance()
                        .getAppId(), bindName, null);
                message.setBindName(bindName);
                mNotifier.bind(bindName, callback);
                return mService.send(message, mEngineServiceListener);
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
        
    }

    public static boolean unbind(String bindName, TSBEngineBindCallback callback) {
        if (mService != null) {
            try {
                RawMessage message = new RawMessage(EngineConfig.instance()
                        .getAppId(), "", "");
                message.setBindName(bindName);
                return mService.send(message, mEngineServiceListener);
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
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
                mService.addEngineInterface(EngineConfig.instance().getAppId());
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

    public static interface TSBEngineListener {
        public void call(RawMessage msg);
    }

    private static EngineServiceListener mEngineServiceListener = new EngineServiceListener.Stub() {

        @Override
        public void call(RawMessage value) throws RemoteException {
            mDataPipeline.receive(value);
        }
    };
}