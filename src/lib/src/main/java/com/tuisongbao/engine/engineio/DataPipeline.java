package com.tuisongbao.engine.engineio;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.tuisongbao.engine.engineio.exception.DataSinkException;
import com.tuisongbao.engine.engineio.sink.IEngineDataSink;
import com.tuisongbao.engine.engineio.source.IEngineCallback;
import com.tuisongbao.engine.engineio.source.IEngineDataSource;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.service.RawMessage;

import org.json.JSONObject;

/**
 * A pipeline that ferries data from IEngineDataSources to EngineDataSinks.
 *
 */
public class DataPipeline implements IEngineCallback {
    private static final String TAG = DataPipeline.class.getSimpleName();
    private CopyOnWriteArrayList<IEngineDataSink> mSinks =
            new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<IEngineDataSource> mSources =
            new CopyOnWriteArrayList<>();

    /**
     * Accept new values from data sources and send it out to all registered
     * sinks.
     *
     * This method is required to implement the SourceCallback interface.
     *
     * If any data sink throws a DataSinkException when receiving data, it will
     * be removed from the list of sinks.
     */
    public void receive(RawMessage message) {
        if(message == null) {
            return;
        }
        List<IEngineDataSink> deadSinks = new ArrayList<IEngineDataSink>();
        for(Iterator<IEngineDataSink> i = mSinks.iterator(); i.hasNext();) {
            IEngineDataSink sink = i.next();
            try {
                sink.receive(message);
            } catch(DataSinkException e) {
                LogUtil.warn(LogUtil.LOG_TAG_ENGINEIO, TAG + ": The sink " +
                        sink + " exploded when we sent a new message " +
                        "-- removing it from the pipeline: " + e);
                deadSinks.add(sink);
            }
        }
        for(IEngineDataSink sink : deadSinks) {
            removeSink(sink);
        }
    }

    /**
     * Add a new sink to the pipeline.
     */
    public IEngineDataSink addSink(IEngineDataSink sink) {
        mSinks.add(sink);
        return sink;
    }

    /**
     * Remove a previously added sink from the pipeline.
     *
     * @param sink if the value is null, it is ignored.
     */
    public void removeSink(IEngineDataSink sink) {
        if(sink != null) {
            mSinks.remove(sink);
            sink.stop();
        }
    }

    /**
     * Add a new source to the pipeline.
     *
     * The source is given a reference to this DataPipeline as its callback.
     */
    public IEngineDataSource addSource(IEngineDataSource source) {
        source.setCallback(this);
        mSources.add(source);
        return source;
    }

    /**
     * Remove a previously added source from the pipeline.
     *
     * Once removed, the source should no longer use this pipeline for its
     * callback.
     *
     * @param source if the value is null, it is ignored.
     */
    public void removeSource(IEngineDataSource source) {
        if(source != null) {
            mSources.remove(source);
            source.stop();
        }
    }

    public List<IEngineDataSource> getSources() {
        return mSources;
    }

    public List<IEngineDataSink> getSinks() {
        return mSinks;
    }

    /**
     * Clear all sources and sinks from the pipeline and stop all of them.
     */
    public void stop() {
        clearSources();
        clearSinks();
    }

    /**
     * Remove and stop all sources in the pipeline.
     */
    public void clearSources() {
        for(Iterator<IEngineDataSource> i = mSources.iterator(); i.hasNext();) {
            (i.next()).stop();
        }
        mSources.clear();
    }

    /**
     * Remove and stop all sinks in the pipeline.
     */
    public void clearSinks() {
        for(Iterator<IEngineDataSink> i = mSinks.iterator(); i.hasNext();) {
            (i.next()).stop();
        }
        mSinks.clear();
    }

    @Override
    public void receive(JSONObject message) {

    }
}
