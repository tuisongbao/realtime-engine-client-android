package com.tuisongbao.engine;

import android.content.Context;

import com.tuisongbao.engine.channel.ChannelManager;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.connection.AutoReconnectConnection;
import com.tuisongbao.engine.engineio.pipeline.TSBEnginePipeline;
import com.tuisongbao.engine.engineio.sink.TSBEngineDataSink;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.StrUtil;

public final class TSBEngine {
    private static final String TAG = TSBEngine.class.getSimpleName();

    private AutoReconnectConnection connection;
    private ChatManager chatManager;
    private ChannelManager channelManager;
    private TSBEngineDataSink sink;
    private TSBEnginePipeline pipeline = new TSBEnginePipeline();

    private static Context mApplicationContext = null;
    private TSBEngineOptions mEngineOptions;
    private String mPushAppId;
    private String mPushToken;
    private String mPushService;

    public TSBEngine(Context context, TSBEngineOptions options) {

        // save the application context
        mApplicationContext = context.getApplicationContext();
        try {
            mEngineOptions = options;
            if (options == null || StrUtil.isEmpty(mEngineOptions.getAppId())) {
                LogUtil.warn(TAG
                        , "No AppId, you do not have permission to use cool engine!");
                return;
            }

            connection = new AutoReconnectConnection(this);
            sink = new TSBEngineDataSink(this);
            pipeline.addSource(connection);
            pipeline.addSink(sink);

            if (StrUtil.isEmpty(mEngineOptions.getAuthEndpoint())) {
                LogUtil.warn(TAG
                        , "No auth endpoint, you only can subscribe public channel, and can not implement cool Chat!");
                channelManager = new ChannelManager(this);
                return;
            } else if (mEngineOptions.getChatIntentService() == null) {
                LogUtil.warn(TAG
                        , "No Intent service specified for receiving chat messages, " +
                            "you only can use Pub/Sub feature, if this is what you want, ignore this warning!");
                // TODO: Init ChannelManager
                channelManager = new ChannelManager(this);
            } else {
                LogUtil.info(TAG,
                        "Successfully load configurations for engine.");
                // TODO: Init ChatManager and ChannelManager
                chatManager = new ChatManager(this);
            }
            // TODO: When success connected, bind push if chat is enabled

        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
    }

    public static Context getContext() {
        return mApplicationContext;
    }

    public TSBEngineOptions getEngineOptions() {
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

    public TSBEngineDataSink getSink() {
        return sink;
    }

    public TSBEnginePipeline getPipeline() {
        return pipeline;
    }
}
