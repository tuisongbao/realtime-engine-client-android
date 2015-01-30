package com.tuisongbao.android.engine.engineio.exception;

import com.tuisongbao.android.engine.common.BaseEngineException;

public class DataSourceException extends BaseEngineException {

    private static final long serialVersionUID = 1L;

    public DataSourceException(int theCode, String theMessage) {
        super(theCode, theMessage);
    }

    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSourceException(Throwable cause) {
        super(cause);
    }

}
