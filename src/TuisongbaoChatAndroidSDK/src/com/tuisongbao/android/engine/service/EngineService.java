package com.tuisongbao.android.engine.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.tuisongbao.android.engine.engineio.EngineIOManager;

public class EngineService extends Service {

    /**
     * Binder to connect IBinder in a ServiceConnection with the VehicleManager.
     * 
     * This class is used in the onServiceConnected method of a
     * ServiceConnection in a client of this service - the IBinder given to the
     * application can be cast to the VehicleBinder to retrieve the actual
     * service instance. This is required to actaully call any of its methods.
     */
    public class EngineServiceBinder extends Binder {
        /**
         * Return this Binder's parent VehicleManager instance.
         * 
         * @return an instance of VehicleManager.
         */
        public EngineService getService() {
            return EngineService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EngineIOManager.getInstance().init(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private boolean send(RawMessage message, EngineServiceListener l) {
        return EngineIOManager.getInstance().send(message, l);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EngineIOManager.getInstance().stop();
    }

    private final EngineServiceInterface.Stub mBinder = new EngineServiceInterface.Stub() {

        /**
         * 发送消息到事实引擎
         * 
         * @param message
         * @return
         */
        public boolean send(RawMessage message, EngineServiceListener l) {
            return EngineService.this.send(message, l);
        }
    };

}
