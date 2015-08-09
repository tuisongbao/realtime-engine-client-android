package com.tuisongbao.engine;

import android.content.Context;

import com.tuisongbao.engine.channel.ChannelManager;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.connection.AutoReconnectConnection;
import com.tuisongbao.engine.engineio.pipeline.EnginePipeline;
import com.tuisongbao.engine.engineio.sink.EngineDataSink;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.utils.StrUtils;

public final class Engine {
    private static final String TAG = "TSB" + Engine.class.getSimpleName();

    private AutoReconnectConnection connection;
    private ChatManager chatManager;
    private ChannelManager channelManager;
    private EngineDataSink sink;
    private EnginePipeline pipeline = new EnginePipeline();

    private static Context mApplicationContext = null;
    private EngineOptions mEngineOptions;

    public Engine(Context context, EngineOptions options) {

        // save the application context
        mApplicationContext = context.getApplicationContext();
        try {
            mEngineOptions = options;
            if (options == null || StrUtils.isEmpty(mEngineOptions.getAppId())) {
                LogUtil.warn(TAG
                        , "No AppId, you do not have permission to use cool engine!");
                return;
            }

            connection = new AutoReconnectConnection(this);
            sink = new EngineDataSink(this);
            pipeline.addSource(connection);
            pipeline.addSink(sink);

            if (StrUtils.isEmpty(mEngineOptions.getAuthEndpoint())) {
                LogUtil.warn(TAG
                        , "No auth endpoint, you only can subscribe public channel, and can not implement cool Chat!");
                channelManager = new ChannelManager(this);
                return;
            } else {
                LogUtil.info(TAG,
                        "Successfully load configurations for engine.");
                chatManager = new ChatManager(this);
                channelManager = new ChannelManager(this);
            }

        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
    }

    public static Context getContext() {
        return mApplicationContext;
    }

    public EngineOptions getEngineOptions() {
        return mEngineOptions;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public AutoReconnectConnection getConnection() {
        return connection;
    }

    public EngineDataSink getSink() {
        return sink;
    }

    public EnginePipeline getPipeline() {
        return pipeline;
    }

    public ResponseError getUnhandledResponseError() {
        ResponseError error = new ResponseError();
        error.setMessage("Unhandled exception occur!");
        return error;
    }
}
