package com.tuisongbao.engine.task;

import com.tuisongbao.engine.common.BaseEngineException;


public class HttpURLConnectionCallBack implements IFeedBackSink<String>
{
    public void done(String result, BaseEngineException e) {
    }

    @Override
    public void internalDone(String paramT,
            BaseEngineException paramParseException) {
        done(paramT, paramParseException);
    }
}