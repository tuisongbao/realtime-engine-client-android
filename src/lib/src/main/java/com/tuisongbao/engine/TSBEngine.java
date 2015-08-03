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
    public static AutoReconnectConnection connection;
    public static ChatManager chatManager;
    public static ChannelManager channelManager;
    public static TSBEngineDataSink sink;
    private TSBEnginePipeline mTSBEnginePipeline = new TSBEnginePipeline();

    private static final String TAG = TSBEngine.class.getSimpleName();

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
            mTSBEnginePipeline.addSource(connection);
            mTSBEnginePipeline.addSink(sink);

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
}
