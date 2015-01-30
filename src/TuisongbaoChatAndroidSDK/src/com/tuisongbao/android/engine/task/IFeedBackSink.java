package com.tuisongbao.android.engine.task;

import com.tuisongbao.android.engine.common.BaseEngineException;

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
    void internalDone(T paramT, BaseEngineException paramParseException);
}
