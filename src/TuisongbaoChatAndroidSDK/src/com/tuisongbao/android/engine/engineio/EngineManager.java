package com.tuisongbao.android.engine.engineio;

import java.util.concurrent.ConcurrentHashMap;

import com.tuisongbao.android.engine.engineio.exception.DataSinkException;
import com.tuisongbao.android.engine.engineio.interfaces.EngineIoInterface;
import com.tuisongbao.android.engine.engineio.interfaces.IEngineInterface;
import com.tuisongbao.android.engine.engineio.source.IEngineDataSource;
import com.tuisongbao.android.engine.service.RawMessage;

public class EngineManager {

    private static EngineManager mInstance;
    private EngineIoInterface mInterface;

    public static EngineManager getInstance() {
        if (mInstance == null) {
            mInstance = new EngineManager();
        }
        return mInstance;
    }

    public IEngineDataSource init(EngineIoOptions options) {
        mInterface = new EngineIoInterface(options);
        return mInterface;
    }

    public boolean isConnected() {
        return mInterface.isConnected();
    }

    public String getSocketId() {
        return mInterface.getSocketId();
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
