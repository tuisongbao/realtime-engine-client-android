package com.tuisongbao.engine;

import android.content.Context;

import com.tuisongbao.engine.channel.TSBChannelManager;
import com.tuisongbao.engine.chat.TSBChatManager;
import com.tuisongbao.engine.connection.AutoReconnectConnection;
import com.tuisongbao.engine.engineio.DataPipeline;
import com.tuisongbao.engine.engineio.sink.TSBListenerSink;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.StrUtil;

public final class TSBEngine {
    public static AutoReconnectConnection connection;
    public static TSBChatManager chatManager;
    public static TSBChannelManager channelManager;
    public static TSBListenerSink sink;
    private DataPipeline mDataPipeline = new DataPipeline();

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
                LogUtil.warn(LogUtil.LOG_TAG_TSB_ENGINE
                        , "No AppId, you do not have permission to use cool engine!");
                return;
            }

            connection = new AutoReconnectConnection(this);
            sink = new TSBListenerSink(this);
            mDataPipeline.addSource(connection);
            mDataPipeline.addSink(sink);

            if (StrUtil.isEmpty(mEngineOptions.getAuthEndpoint())) {
                LogUtil.warn(LogUtil.LOG_TAG_TSB_ENGINE
                        , "No auth endpoint, you only can subscribe public channel, and can not implement cool Chat!");
                channelManager = new TSBChannelManager(this);
                return;
            } else if (mEngineOptions.getChatIntentService() == null) {
                LogUtil.warn(LogUtil.LOG_TAG_TSB_ENGINE
                        , "No Intent service specified for receiving chat messages, " +
                            "you only can use Pub/Sub feature, if this is what you want, ignore this warning!");
                // TODO: Init ChannelManager
                channelManager = new TSBChannelManager(this);
            } else {
                LogUtil.info(LogUtil.LOG_TAG_TSB_ENGINE,
                        "Successfully load configurations for engine.");
                // TODO: Init ChatManager and ChannelManager
                chatManager = new TSBChatManager(this);
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
