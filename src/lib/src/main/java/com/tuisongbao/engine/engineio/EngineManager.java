package com.tuisongbao.engine.engineio;

import com.tuisongbao.engine.engineio.interfaces.EngineIoInterface;
import com.tuisongbao.engine.engineio.source.IEngineDataSource;
import com.tuisongbao.engine.service.RawMessage;

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

    private EngineManager() {
        // empty
    }
}
