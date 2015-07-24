package com.tuisongbao.engine.engineio.sink;

import com.tuisongbao.engine.engineio.exception.DataSinkException;
import com.tuisongbao.engine.service.RawMessage;

/**
 * The interface for all engine data destination endpoints.
 *
 */
public interface IEngineDataSink {
    /**
     * Receive a data point with a name, a value and a event value.
     *
     *
     * @param message The new measurement.
     */
    public boolean receive(RawMessage message) throws DataSinkException;

    /**
     * Release any acquired resources and either stop sending message (if a
     * source) or stop expecting to receive them (if a sink).
     */
    public void stop();
}
