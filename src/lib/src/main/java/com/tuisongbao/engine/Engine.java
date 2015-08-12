package com.tuisongbao.engine;

import android.content.Context;

import com.tuisongbao.engine.channel.ChannelManager;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.connection.AutoReconnectConnection;
import com.tuisongbao.engine.connection.Connection;
import com.tuisongbao.engine.engineio.pipeline.EnginePipeline;
import com.tuisongbao.engine.engineio.sink.EngineDataSink;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.utils.StrUtils;

/**
 * <STRONG>推送宝实时引擎客户端 SDK 的入口</STRONG>
 *
 * <P>
 *     通过 {@link EngineOptions} 初始化该类。
 *     初始化过程中会创建 {@link AutoReconnectConnection}，{@link ChatManager} 和 {@link ChannelManager} 的实例，有且只有一个。
 *     支持单个应用中多个实例。
 *
 * @author Katherine Zhu
 */
public final class Engine {
    private static final String TAG = "TSB" + Engine.class.getSimpleName();

    private AutoReconnectConnection connection;
    private ChatManager chatManager;
    private ChannelManager channelManager;
    private EngineDataSink sink;
    private EnginePipeline pipeline = new EnginePipeline();

    private static Context mApplicationContext = null;
    private EngineOptions mEngineOptions;

    /**
     * 以 options 的配置实例化 Engine, 创建并建立连接，初始化 {@link ChatManager} 和 {@link ChannelManager}
     *
     * @param context 应用级别的 Context
     * @param options Engine 的配置
     */
    public Engine(Context context, EngineOptions options) {
        // Save the application context
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

    /**
     * 获取 Engine 的配置
     *
     * @return 当前的配置
     */
    public EngineOptions getEngineOptions() {
        return mEngineOptions;
    }

    /**
     * 获取 ChatManager 的实例
     *
     * @return {@code ChatManager} 实例
     */
    public ChatManager getChatManager() {
        return chatManager;
    }

    /**
     * 获取 ChannelManager 的实例
     *
     * @return {@code ChannelManager} 实例
     */
    public ChannelManager getChannelManager() {
        return channelManager;
    }

    /**
     * 获取 Connection 的实例
     *
     * @return {@code Connection} 实例
     */
    public Connection getConnection() {
        return connection;
    }

    public EngineDataSink getSink() {
        return sink;
    }

    public ResponseError getUnhandledResponseError() {
        ResponseError error = new ResponseError();
        error.setMessage("Unhandled exception occur!");
        return error;
    }
}
