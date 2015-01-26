package com.tuisongbao.android.engine.task;

import com.tuisongbao.android.engine.common.PushException;
import com.tuisongbao.android.engine.http.response.BaseResponse;

public abstract class HttpCallBack implements IFeedBackSink<BaseResponse>
{
    public abstract void done(BaseResponse respone, PushException e);

    public final void internalDone(BaseResponse respone, PushException e)
    {
        done(respone, e);
    }
}
