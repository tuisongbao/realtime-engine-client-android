package com.tuisongbao.engine.engineio.interfaces;

import com.tuisongbao.engine.engineio.exception.DataSourceException;
import com.tuisongbao.engine.engineio.sink.IEngineDataSink;
import com.tuisongbao.engine.engineio.source.IEngineDataSource;


/**
 * A connection to a data interface that is capable of full duplex
 * communication.
 *
 */
public interface IEngineInterface extends IEngineDataSource, IEngineDataSink {

    /**
     * Change the resource used by the instance to connect to the interface,
     * restarting any neccessary services.
     *
     * @param resource The new resource to use for the interface.
     * @return true if the resource was different and the interface was
     *      restarted.
     */
    public boolean setResource(String resource) throws DataSourceException;

    /**
     * Checks whether interface connects to data interface successfully.
     *
     * @return true if the interface has connected to data interface successfully, or false.
     */
    public boolean isConnected();
}
