package com.tuisongbao.engine.engineio.sink;

import com.tuisongbao.engine.common.Event;
import com.tuisongbao.engine.engineio.exception.DataSinkException;

/**
 * The interface for all engine data destination endpoints.
 *
 */
public interface IEngineDataSink {
    /**
     * Receive a data point with a name, a value and a event value.
     *
     *
     * @param event The new measurement.
     */
    boolean receive(Event event) throws DataSinkException;

    /**
     * Release any acquired resources and either stop sending event (if a
     * source) or stop expecting to receive them (if a sink).
     */
    void stop();
}
