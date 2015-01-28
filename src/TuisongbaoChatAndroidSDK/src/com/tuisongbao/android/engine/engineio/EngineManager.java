package com.tuisongbao.android.engine.engineio;

import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;

import com.tuisongbao.android.engine.EngineConfig;
import com.tuisongbao.android.engine.engineio.exception.DataSinkException;
import com.tuisongbao.android.engine.engineio.interfaces.EngineIoInterface;
import com.tuisongbao.android.engine.engineio.interfaces.IEngineInterface;
import com.tuisongbao.android.engine.engineio.source.IEngineDataSource;
import com.tuisongbao.android.engine.service.RawMessage;

public class EngineManager {

    private static EngineManager mInstance;
    private EngineIoInterface mInterface;
    /**
     * Application context
     */
    private Context mContext;

    public static EngineManager getInstance() {
        if (mInstance == null) {
            mInstance = new EngineManager();
        }
        return mInstance;
    }

    public IEngineDataSource init(Context context) {
        mContext = context;
        mInterface = new EngineIoInterface(mContext, EngineConfig.instance()
                .getAppId(), EngineConfig.instance().getAppKey());
        return mInterface;
    }

    public boolean isConnected() {
        return mInstance.isConnected();
    }

    public boolean send(RawMessage message) {
        return mInterface.receive(message);
    }

    public static boolean send(ConcurrentHashMap<String, IEngineInterface> map,
            RawMessage message) {
        IEngineInterface engineInterface = map.get(message.getAppId());
        if (engineInterface != null) {
            try {
                return engineInterface.receive(message);
            } catch (DataSinkException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    private EngineManager() {
        // empty
    }
}
