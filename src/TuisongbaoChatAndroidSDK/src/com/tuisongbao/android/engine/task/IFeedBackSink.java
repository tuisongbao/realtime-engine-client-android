package com.tuisongbao.android.engine.task;

import com.tuisongbao.android.engine.common.PushException;

/**
 * General-purpose callback interface.
 */
public interface IFeedBackSink<T>
{
    /**
     * 
     * @param paramT 
     * @param paramParseException
     */
    void internalDone(T paramT, PushException paramParseException);
}
