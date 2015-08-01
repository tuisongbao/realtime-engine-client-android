package com.tuisongbao.engine.connection.entity;

public class ConnectionEventData {

    private int code;
    private String message;
    private String reconnectStrategy;
    private int reconnectIn;
    private int reconnectInMax;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getReconnectIn() {
        return reconnectIn;
    }

    public String getReconnectStrategy() {
        return reconnectStrategy;
    }

    public int getReconnectInMax() {
        return reconnectInMax;
    }
}
