package com.tuisongbao.android.engine.common;


public abstract class BaseTSBResponseMessage implements ITSBResponseMessage {

    private String mErrorMessage;
    private int mCode;
    private String mName;
    private String mData;
    private String mChannel;
    private String mBindName;

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
    public boolean isSuccess() {
        return mCode == 0;
    }
}
