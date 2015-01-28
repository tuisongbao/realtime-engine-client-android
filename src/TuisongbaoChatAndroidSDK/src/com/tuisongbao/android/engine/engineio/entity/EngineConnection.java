package com.tuisongbao.android.engine.engineio.entity;

import com.tuisongbao.android.engine.entity.TSBEngineConstants;

public class EngineConnection {

    private int code;
    private String message;
    private String socketId;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return code == TSBEngineConstants.TSBENGINE_CONNECTION_CODE_SUCCESS;
    }

    public String getSocketId() {
        return socketId;
    }

    public void setSocketId(String socketId) {
        this.socketId = socketId;
    }
}
