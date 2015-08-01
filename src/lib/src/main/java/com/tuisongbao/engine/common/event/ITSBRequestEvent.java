package com.tuisongbao.engine.common.event;

public interface ITSBRequestEvent {
    void setName(String name);
    String getName();
    Object getData();
    String serialize();
}
