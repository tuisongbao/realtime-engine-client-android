package com.tuisongbao.android.engine.common;

public interface ITSBResponseMessage {

    public void setErrorMessage(String error);
    public void setCode(int code);
    public void setName(String name);
    public void setData(String data);
    public void setChannel(String channel);
    public void setBindName(String bindName);
    public void setServerRequestId(long serverRequestId);
    public boolean isSuccess();
    public void setCallback(ITSBEngineCallback callback);
    public ITSBEngineCallback getCallback();
    public void callBack();
}
