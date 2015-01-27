package com.tuisongbao.android.engine.common;


public abstract class BaseTSBResponseMessage<T> implements ITSBResponseMessage {

    private String mErrorMessage;
    private int mCode;
    private String mName;
    private String mData;
    private String mChannel;
    private TSBEngineCallback<T> mCallBack;

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

    public TSBEngineCallback<T> getCallBack() {
        return mCallBack;
    }

    public void setCallBack(TSBEngineCallback<T> callback) {
        mCallBack = callback;
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
    public boolean isSuccess() {
        return mCode == 0;
    }

    abstract public T parse();

    @Override
    public void callBack() {
        if (isSuccess()) {
            if (mCallBack != null) {
                mCallBack.onSuccess(parse());
            }
        } else {
            if (mCallBack != null) {
                mCallBack.onError(mCode, mErrorMessage);
            }
        }
    }
}
