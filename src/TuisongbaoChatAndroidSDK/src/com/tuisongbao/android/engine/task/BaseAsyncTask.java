package com.tuisongbao.android.engine.task;

import android.os.AsyncTask;

import com.tuisongbao.android.engine.common.PushException;

public abstract class BaseAsyncTask<T> extends AsyncTask<Void, Void, Void>
{
    private final IFeedBackSink<T> mCallBack;
    private T mResult;
    private PushException mException;

    public BaseAsyncTask(IFeedBackSink<T> theCallback)
    {
        this.mResult = null;
        this.mException = null;
        this.mCallBack = theCallback;
    }

    public abstract T run() throws PushException;

    @Override
    protected Void doInBackground(Void... params)
    {
        try
        {
            this.mResult = run();
            return null;
        }
        catch (PushException e)
        {
            this.mException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v)
    {
        if (this.mCallBack != null) this.mCallBack.internalDone(this.mResult, this.mException);
    }

    void executeInThisThread()
    {
        doInBackground(new Void[0]);
        onPostExecute(null);
    }

    public synchronized static int executeTask(BaseAsyncTask<?> task)
    {
        task.execute(new Void[0]);
        return 0;
    }

}
