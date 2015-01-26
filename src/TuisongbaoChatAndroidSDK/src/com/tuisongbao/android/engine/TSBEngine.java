package com.tuisongbao.android.engine;

import java.util.concurrent.ConcurrentHashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.service.EngineService;
import com.tuisongbao.android.engine.service.EngineServiceInterface;
import com.tuisongbao.android.engine.service.EngineServiceListener;
import com.tuisongbao.android.engine.service.RawMessage;

public final class TSBEngine {

    private static Context mApplicationContext = null;
    private static EngineServiceInterface mService;
    private static ConcurrentHashMap<String, TSBEngineListener> mTBSEngineListener = new ConcurrentHashMap<String, TSBEngine.TSBEngineListener>();

    private TSBEngine() {
        // empty here
    }

    /**
     * Open API for Push SDK integration.
     *
     * @param context
     */
    public static void init(Context context, String appId) {

        mApplicationContext = context.getApplicationContext(); // save the application context
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
    public static boolean send(String name, String data, final TSBEngineListener l) {
        if (mService != null) {
            try {
                RawMessage message = new RawMessage(EngineConfig.instance().getAppId(), name, data);
//                mTBSEngineListener.put(message.getUUID(), l);
                return mService.send(message, new EngineServiceListener() {
                    
                    @Override
                    public IBinder asBinder() {
                        return mService.asBinder();
                    }
                    
                    @Override
                    public void call(RawMessage value) throws RemoteException {
                        if (l != null) {
                            l.call(value);
                        }
                    }
                });
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
//            context.startService(i);
            context.bindService(i, mConnection, Context.BIND_AUTO_CREATE);

        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_PUSH_MANAGER, e);
        }
    }

    private static ServiceConnection mConnection = new ServiceConnection()
    {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            LogUtil.debug(LogUtil.LOG_TAG_PUSH_MANAGER, "EngineService success");
            mService = EngineServiceInterface.Stub.asInterface(service);
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className)
        {
            LogUtil.debug(LogUtil.LOG_TAG_PUSH_MANAGER, "EngineService disconnected unexpectedly");
            mService = null;
        }
    };
    
    public static interface TSBEngineListener {
        public void call(RawMessage msg);
    }
}