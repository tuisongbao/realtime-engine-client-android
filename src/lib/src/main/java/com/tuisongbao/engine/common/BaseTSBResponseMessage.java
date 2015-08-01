package com.tuisongbao.engine.common;

import com.google.gson.Gson;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.log.LogUtil;


public abstract class BaseTSBResponseMessage<T> implements ITSBResponseMessage {
    transient protected TSBEngine mEngine;

    private String mErrorMessage;
    private int mCode;
    private String mName;
    private String mData;
    private String mChannel;
    private String mBindName;
    private Object mRequestData;
    private long mServerRequestId;
    private ITSBEngineCallback mCallback;

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public int getCode() {
        return mCode;
    }

    public String getName() {
        return mName;
    }

    public String getData() {
        return mData;
    }

    public String getChannel() {
        return mChannel;
    }

    public String getBindName() {
        return mBindName;
    }

    public long getServerRequestId() {
        return mServerRequestId;
    }

    public Object getRequestData() {
        return mRequestData;
    }

    @Override
    public void setCallback(ITSBEngineCallback callback) {
        mCallback = callback;
    }

    @Override
    public ITSBEngineCallback getCallback() {
        return mCallback;
    }

    @Override
    public void setErrorMessage(String error) {
        mErrorMessage = error;
    }

    @Override
    public void setCode(int code) {
        mCode = code;
    }

    @Override
    public void setName(String name) {
        mName = name;
    }

    @Override
    public void setData(String data) {
        mData = data;
    }

    @Override
    public void setChannel(String channel) {
        mChannel = channel;
    }

    @Override
    public void setBindName(String bindName) {
        mBindName = bindName;
    }

    @Override
    public void setServerRequestId(long serverRequestId) {
        mServerRequestId = serverRequestId;
    }

    @Override
    public void setRequestData(Object data) {
        mRequestData = data;
    }

    @Override
    public boolean isSuccess() {
        return mCode == 0;
    }

    abstract public T parse();

    /**
     * Called before callback event
     *
     * do things like persistent data.
     * @return TODO
     */
    protected T prepareCallBackData() {
        return parse();
    }

    public void callback(Event request, ResponseEventData response) {
        // TODO: 15-8-1 Directly call parse with data, remove set/get Data method
        setData(response.getResult().toString());

        ITSBEngineCallback callBack = getCallback();
        if (response.getOk()) {
            ((TSBEngineCallback)callBack).onSuccess(prepareCallBackData());
        } else {
            ResponseError error = new Gson().fromJson(response.getError(), ResponseError.class);
            ((TSBEngineCallback) callBack).onError(error.getCode(), error.getMessage());
        }
    }

    @Override
    public void callBack() {
        try {
            ITSBEngineCallback callBack = getCallback();
            if (isSuccess()) {
                if (callBack != null) {
                    if (callBack instanceof TSBEngineBindCallback) {
                        String data = getData();
                        if (data == null) {
                            data = Protocol.genErrorJsonString(getCode(), getErrorMessage());
                        }
                        ((TSBEngineBindCallback)callBack).onEvent(getBindName(), getName(), data);
                    }
                    if (callBack instanceof TSBEngineCallback) {
                        ((TSBEngineCallback)callBack).onSuccess(prepareCallBackData());
                    }
                }
            } else {
                if (callBack != null) {
                    if (callBack instanceof TSBEngineBindCallback) {
                        String data = getData();
                        if (data == null) {
                            data = Protocol.genErrorJsonString(getCode(), getErrorMessage());
                        }
                        ((TSBEngineBindCallback)callBack).onEvent(getBindName(), getName(), getData());
                    }
                    if (callBack instanceof TSBEngineCallback) {
                        ((TSBEngineCallback)callBack).onError(getCode(), getErrorMessage());
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_CHAT, e);
        }

    }
}
