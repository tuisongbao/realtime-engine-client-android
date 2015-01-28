package com.tuisongbao.android.engine.channel;

import com.google.gson.Gson;
import com.tuisongbao.android.engine.EngineConfig;
import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.channel.entity.TSBChannelSubscribeData;
import com.tuisongbao.android.engine.channel.entity.TSBChannelUserData;
import com.tuisongbao.android.engine.channel.message.TSBSubscribeMessage;
import com.tuisongbao.android.engine.common.BaseManager;
import com.tuisongbao.android.engine.common.TSBEngineBindCallback;
import com.tuisongbao.android.engine.entity.TSBEngineConstants;
import com.tuisongbao.android.engine.util.HashHmacUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBChannelManager extends BaseManager {

    private static TSBChannelManager mInstance;

    public static TSBChannelManager getInstance() {
        if (mInstance == null) {
            mInstance = new TSBChannelManager();
        }
        return mInstance;
    }

    private TSBChannelManager() {
        // empty
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
        if (StrUtil.isEmpty(channel)) {
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
                .startsWith(TSBEngineConstants.TSBENGINE_CHANNEL_PREFIX_PRIVATE)) {
            if (userData != null) {
                // Private Channel
                data.setChannelData(userData);
                // <socketId>:<channelName>:<JSON encoded user data>
                String strToSign = TSBEngine.getSocketId() + ":" + channel + ":" + new Gson().toJson(userData);
                String signature = HashHmacUtil.hmacSHA256(strToSign, EngineConfig.instance().getAppKey());
                data.setSignature(signature);
            } else {
                return;
            }
        } else {
            // empty
        }
        msg.setData(data);
        send(msg);
    }

    public void bind(String bindName, TSBEngineBindCallback callback) {
        super.bind(bindName, callback);
    }

    public void unbind(String bindName) {
        super.unbind(bindName);
    }
}
