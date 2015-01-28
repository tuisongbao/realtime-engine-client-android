package com.tuisongbao.android.engine.channel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.google.gson.Gson;
import com.tuisongbao.android.engine.EngineConfig;
import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.channel.entity.ChannelState;
import com.tuisongbao.android.engine.channel.entity.TSBChannelSubscribeData;
import com.tuisongbao.android.engine.channel.entity.TSBChannelUserData;
import com.tuisongbao.android.engine.channel.message.TSBSubscribeMessage;
import com.tuisongbao.android.engine.common.BaseManager;
import com.tuisongbao.android.engine.common.TSBEngineBindCallback;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.connection.entity.TSBConnection;
import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.entity.TSBEngineConstants;
import com.tuisongbao.android.engine.util.HashHmacUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBChannelManager extends BaseManager {
    
    Map<String, TSBSubscribeMessage> mChannelMap = new HashMap<String, TSBSubscribeMessage>();

    private static TSBChannelManager mInstance;

    public static TSBChannelManager getInstance() {
        if (mInstance == null) {
            mInstance = new TSBChannelManager();
        }
        return mInstance;
    }

    private TSBChannelManager() {
        // empty
        bindConnectionEvents();
    }

    public void subscribe(String channel) {
        subscribe(channel, null);
    }

    /**
     * 用于订阅channel，如需要订阅Private Channel，名字必须使用 private- 前缀
     * {@link TSBEngineConstants#TSBENGINE_CHANNEL_PREFIX_PRIVATE}，如需要订阅Presence
     * Channel，名字必须使用 presence- 前缀
     * {@link TSBEngineConstants#TSBENGINE_CHANNEL_PREFIX_PRESENCE}。
     * 
     * @param channel
     * @param signature
     * @param userData
     * @param callback
     */
    public void subscribe(String channel, TSBChannelUserData userData) {
        if (StrUtil.isEmpty(channel) || hasSubscribe(channel)) {
            return;
        }
        TSBSubscribeMessage msg = new TSBSubscribeMessage();
        TSBChannelSubscribeData data = new TSBChannelSubscribeData();
        data.setChannel(channel);
        if (channel
                .startsWith(TSBEngineConstants.TSBENGINE_CHANNEL_PREFIX_PRIVATE)) {
            // Private Channel
            // TODO: Need check whether is connected
            // 获取签名 <socketId>:<channelName>
            String strToSign = TSBEngine.getSocketId() + ":" + channel;
            String signature = HashHmacUtil.hmacSHA256(strToSign, EngineConfig.instance().getAppKey());
             data.setSignature(signature);
        } else if (channel
                .startsWith(TSBEngineConstants.TSBENGINE_CHANNEL_PREFIX_PRESENCE)) {
            if (userData != null) {
                // Private Channel
                data.setChannelData(userData);
                // <socketId>:<channelName>:<JSON encoded user data>
                String str = new String(new Gson().toJson(userData));
                String strToSign = TSBEngine.getSocketId() + ":" + channel + ":" + str;
                String signature = HashHmacUtil.hmacSHA256(strToSign, EngineConfig.instance().getAppKey());
                data.setSignature(signature);
            } else {
                return;
            }
        } else {
            // empty
        }
        msg.setData(data);
        bindChannelEvents(channel, msg);
        if (TSBEngine.isConnected()) {
            msg.setState(ChannelState.SUBSCRIBE_SENDING);
            send(msg);
        } else {
            // when connected event triggered, sent it
            msg.setState(ChannelState.INITIAL);
        }
    }

    public void bind(String bindName, TSBEngineBindCallback callback) {
        super.bind(bindName, callback);
    }

    public void unbind(String bindName) {
        super.unbind(bindName);
    }
    
    private boolean hasSubscribe(String channel) {
        return mChannelMap.containsKey(channel);
    }

    private void bindConnectionEvents() {
        TSBEngine.connection.bind(TSBEngineConstants.TSBENGINE_EVENT_CONNECTION_CONNECTED, mConnectionCallback);
    }

    private void bindChannelEvents(String channel, TSBSubscribeMessage msg) {
        bind(channel, mSubscribeEngineCallback);
        mChannelMap.put(channel, msg);
    }

    private TSBEngineBindCallback mSubscribeEngineCallback = new TSBEngineBindCallback() {
        
        @Override
        public void onEvent(String eventName, String name, String data) {
            if (!StrUtil.isEmpty(eventName)) {
                TSBSubscribeMessage subscribeMessage = mChannelMap.get(eventName);
                if (subscribeMessage != null) {
                    if (EngineConstants.CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED.equals(name)) {
                        subscribeMessage.setState(ChannelState.SUBSCRIBED);
                    }
                    if (EngineConstants.CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED_ERROR.equals(name)) {
                        subscribeMessage.setState(ChannelState.FAILED);
                    }
                }
            }
        }
    };

    private TSBEngineCallback<TSBConnection> mConnectionCallback = new TSBEngineCallback<TSBConnection>() {

        @Override
        public void onSuccess(TSBConnection t) {
            handleConnect();
        }

        @Override
        public void onError(int code, String message) {
            handeDisconnect();
        }
    };

    private void handleConnect() {
        HashSet<TSBSubscribeMessage> values = new HashSet<TSBSubscribeMessage>(mChannelMap.values());
        for (TSBSubscribeMessage message : values) {
            message.setState(ChannelState.SUBSCRIBE_SENDING);
            send(message);
        }
    }

    private void handeDisconnect() {
        HashSet<TSBSubscribeMessage> values = new HashSet<TSBSubscribeMessage>(mChannelMap.values());
        for (TSBSubscribeMessage message : values) {
            message.setState(ChannelState.UNSUBSCRIBED);
        }
    }
}
