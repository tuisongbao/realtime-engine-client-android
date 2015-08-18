package com.tuisongbao.engine.engineio.pipeline;

import com.tuisongbao.engine.engineio.exception.DataSinkException;
import com.tuisongbao.engine.engineio.sink.IEngineDataSink;
import com.tuisongbao.engine.engineio.source.IEngineDataSource;
import com.tuisongbao.engine.log.LogUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A pipeline that ferries data from IEngineDataSources to EngineDataSinks.
 *
 */
public class EnginePipeline implements IEnginePipeline {
    private static final String TAG = "TSB" + EnginePipeline.class.getSimpleName();
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
    @Override
    public void ferry(String event) {
        List<IEngineDataSink> deadSinks = new ArrayList<>();
        for (IEngineDataSink sink : mSinks) {
            try {
                sink.receive(event);
            } catch (DataSinkException e) {
                LogUtil.warn(TAG, "The sink " +
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
     * The source is given a reference to this EnginePipeline as its onResponse.
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
     * onResponse.
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
        for (IEngineDataSink mSink : mSinks) {
            (mSink).stop();
        }
        mSinks.clear();
    }
}
