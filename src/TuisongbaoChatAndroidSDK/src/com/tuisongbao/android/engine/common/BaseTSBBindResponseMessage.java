package com.tuisongbao.android.engine.common;


public abstract class BaseTSBBindResponseMessage<T> extends BaseTSBResponseMessage {

    private TSBEngineCallback<T> mCallBack;
    private TSBEngineBindCallback mBindCallBack;

    public TSBEngineCallback<T> getCallBack() {
        return mCallBack;
    }

    public void setCallBack(TSBEngineCallback<T> callback) {
        mCallBack = callback;
    }

    public TSBEngineBindCallback getBindCallBack() {
        return mBindCallBack;
    }

    public void setBindCallBack(TSBEngineBindCallback bindCallback) {
        mBindCallBack = bindCallback;
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
        if (mBindCallBack != null) {
            mBindCallBack.onEvent(getBindName(), getName(), getData());
        }
    }
}
