package com.tuisongbao.engine.connection.entity;

import com.tuisongbao.engine.entity.TSBEngineConstants;

public class TSBConnectionEvent {

    private int code;
    private String message;

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
        return code == TSBEngineConstants.TSBENGINE_CODE_SUCCESS;
    }
}
