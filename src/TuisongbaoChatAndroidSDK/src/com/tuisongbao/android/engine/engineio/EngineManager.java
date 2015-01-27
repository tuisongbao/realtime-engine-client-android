package com.tuisongbao.android.engine.engineio;

import java.util.concurrent.ConcurrentHashMap;

import com.tuisongbao.android.engine.engineio.exception.DataSinkException;
import com.tuisongbao.android.engine.engineio.interfaces.IEngineInterface;
import com.tuisongbao.android.engine.service.EngineServiceListener;
import com.tuisongbao.android.engine.service.RawMessage;

public class EngineManager {

    public static boolean send(ConcurrentHashMap<String, IEngineInterface> map,
            RawMessage message, EngineServiceListener listener) {
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
}
