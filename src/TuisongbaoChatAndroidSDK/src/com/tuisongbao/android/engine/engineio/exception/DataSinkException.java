package com.tuisongbao.android.engine.engineio.exception;

import com.tuisongbao.android.engine.common.PushException;

public class DataSinkException extends PushException {

    private static final long serialVersionUID = 1L;

    public DataSinkException(int theCode, String theMessage) {
        super(theCode, theMessage);
    }


    public DataSinkException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSinkException(Throwable cause) {
        super(cause);
    }
}
