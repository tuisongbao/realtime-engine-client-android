package com.tuisongbao.engine.task;

import com.tuisongbao.engine.common.BaseEngineException;

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
