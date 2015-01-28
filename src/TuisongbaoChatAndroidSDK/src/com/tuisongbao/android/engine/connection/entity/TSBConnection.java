package com.tuisongbao.android.engine.connection.entity;

import com.tuisongbao.android.engine.entity.TSBEngineConstants;

public class TSBConnection {

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
        return code == TSBEngineConstants.TSBENGINE_CONNECTION_CODE_SUCCESS;
    }
}
