package com.tuisongbao.android.engine.service;

import java.util.concurrent.ConcurrentHashMap;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.tuisongbao.android.engine.engineio.DataPipeline;
import com.tuisongbao.android.engine.engineio.EngineIOManager;
import com.tuisongbao.android.engine.engineio.EngineManager;
import com.tuisongbao.android.engine.engineio.exception.DataSinkException;
import com.tuisongbao.android.engine.engineio.interfaces.EngineIoInterface;
import com.tuisongbao.android.engine.engineio.interfaces.IEngineInterface;
import com.tuisongbao.android.engine.engineio.sink.ServiceCallbackSink;

public class EngineService extends Service {

    private final static String TAG = EngineService.class.getSimpleName();
    private DataPipeline mDataPipeline = new DataPipeline();
    private ConcurrentHashMap<String, IEngineInterface> mEngineInterfaceMap = new ConcurrentHashMap<String, IEngineInterface>();
    private ServiceCallbackSink mNotifier = new ServiceCallbackSink();

    /**
     * Binder to connect IBinder in a ServiceConnection with the TSBEngine.
     * 
     * This class is used in the onServiceConnected method of a
     * ServiceConnection in a client of this service - the IBinder given to the
     * application can be cast to the EngineServiceBinder to retrieve the actual
     * service instance. This is required to actaully call any of its methods.
     */
    public class EngineServiceBinder extends Binder {
        /**
         * Return this Binder's parent EngineService instance.
         * 
         * @return an instance of EngineService.
         */
        public EngineService getService() {
            return EngineService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        initializeDefaultSinks();
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service being destroyed");
        mDataPipeline.stop();
        EngineIOManager.getInstance().stop();
    }

    private final EngineServiceInterface.Stub mBinder = new EngineServiceInterface.Stub() {

        /**
         * 发送消息到事实引擎
         * 
         * @param message
         * @param listener
         * @return
         */
        @Override
        public boolean send(RawMessage message, EngineServiceListener listener) {
            return EngineService.this.send(message, listener);
        }

        /**
         * 绑定事件
         * 
         * @param message
         * @param listener
         * @return
         * @throws DataSinkException
         */
        @Override
        public void bind(RawMessage message, EngineServiceListener listener) {
            EngineService.this.bind(message, listener);
        }

        /**
         * 绑定事件
         * 
         * @param message
         * @param listener
         * @return
         */
        @Override
        public void unbind(RawMessage message, EngineServiceListener listener) {
            EngineService.this.unbind(message, listener);
        }

        /**
         * 添加消息源
         * 
         * @param message
         * @return
         * @throws DataSinkException
         */
        @Override
        public void addEngineInterface(String appId, String appKey) {
            EngineService.this.addEngineInterface(appId, appKey);
        }
    };

    private void initializeDefaultSinks() {
        mDataPipeline.addSink(mNotifier);
    }

    private boolean send(RawMessage message, EngineServiceListener listener) {
        if (listener != null) {
            mNotifier.register(message, listener);
        } else {
            long requestId = mNotifier.getReqeustId();
            message.setRequestId(requestId);
        }
        return EngineManager.send(mEngineInterfaceMap, message);
    }

    private void bind(RawMessage message, EngineServiceListener listener) {
        mNotifier.bind(message, listener);
    }

    private void unbind(RawMessage message, EngineServiceListener listener) {
        mNotifier.unbind(message, listener);
    }

    private void addEngineInterface(String appId, String appKey) {
        IEngineInterface engineInterface = findActiveEngineInterface(appId);

        if (engineInterface == null) {
            engineInterface = new EngineIoInterface(this, appId, appKey);

            mEngineInterfaceMap.put(appId, engineInterface);
            mDataPipeline.addSource(engineInterface);
        } else {
            // empty
        }
        Log.i(TAG, "Added engine interface  " + engineInterface);
    }

    private IEngineInterface findActiveEngineInterface(String appId) {
        IEngineInterface engineInterface = mEngineInterfaceMap.get(appId);
        return engineInterface;
    }

}
