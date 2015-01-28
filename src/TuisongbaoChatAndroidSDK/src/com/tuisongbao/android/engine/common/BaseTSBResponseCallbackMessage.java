package com.tuisongbao.android.engine.common;


public abstract class BaseTSBResponseCallbackMessage<T> extends BaseTSBResponseMessage {

    private TSBEngineCallback<T> mCallBack;

    public TSBEngineCallback<T> getCallBack() {
        return mCallBack;
    }

    public void setCallBack(TSBEngineCallback<T> callback) {
        mCallBack = callback;
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
                mCallBack.onError(getCode(), getErrorMessage());
            }
        }
    }
}
