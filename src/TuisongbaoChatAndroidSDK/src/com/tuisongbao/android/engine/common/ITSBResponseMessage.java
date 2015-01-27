package com.tuisongbao.android.engine.common;

public interface ITSBResponseMessage {

    public void setErrorMessage(String error);
    public void setCode(int code);
    public void setName(String name);
    public void setData(String data);
    public void setChannel(String channel);
    public boolean isSuccess();
    public void callBack();
}
