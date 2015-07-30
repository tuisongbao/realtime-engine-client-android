package com.tuisongbao.engine.common;

public interface ITSBRequestMessage {
    void setName(String name);
    String getName();
    Object getData();
    String serialize();
}
