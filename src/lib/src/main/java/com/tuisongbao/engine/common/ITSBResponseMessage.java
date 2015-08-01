package com.tuisongbao.engine.common;

public interface ITSBResponseMessage {
    void setErrorMessage(String error);
    void setCode(int code);
    void setName(String name);
    void setData(String data);
    void setChannel(String channel);
    void setBindName(String bindName);
    void setServerRequestId(long serverRequestId);
    void setRequestData(Object params);
    boolean isSuccess();
    void setCallback(ITSBEngineCallback callback);
    ITSBEngineCallback getCallback();
    void callBack();
    void callback(Event request, ResponseEventData response);
}
