package com.tuisongbao.android.engine.common;

public interface ITSBRequestMessage {

    public void setName(String name);
    public String getName();
    public Object getData();
    public String serialize();
}
