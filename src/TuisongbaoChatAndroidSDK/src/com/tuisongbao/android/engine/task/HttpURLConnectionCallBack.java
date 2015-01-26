package com.tuisongbao.android.engine.task;

import com.tuisongbao.android.engine.common.PushException;


public class HttpURLConnectionCallBack implements IFeedBackSink<String>
{
    public void done(String result, PushException e) {
    }

    @Override
    public void internalDone(String paramT,
            PushException paramParseException) {
        done(paramT, paramParseException);
    }
}