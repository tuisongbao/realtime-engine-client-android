package com.tuisongbao.android.engine.channel;

import com.tuisongbao.android.engine.channel.entity.TSBChannelSubscribeData;
import com.tuisongbao.android.engine.channel.entity.TSBChannelUserData;
import com.tuisongbao.android.engine.channel.message.TSBSubscribeMessage;
import com.tuisongbao.android.engine.channel.message.TSBSubscribeResponseMessage;
import com.tuisongbao.android.engine.common.BaseManager;
import com.tuisongbao.android.engine.common.TSBEngineCallback;

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

    public void subscribe(String channel, TSBEngineCallback<String> callback) {
        subscribe(channel, null, null, callback);
    }

    public void subscribe(String channel) {
        subscribe(channel, null, null, null);
    }

    public void subscribe(String channel, String signature,
            TSBChannelUserData userData, TSBEngineCallback<String> callback) {
        TSBSubscribeMessage msg = new TSBSubscribeMessage();
        TSBChannelSubscribeData data = new TSBChannelSubscribeData();
        data.setChannel(channel);
        data.setChannelData(userData);
        data.setSignature(signature);
        msg.setData(data);
        if (callback == null) {
            send(msg);
        } else {
            TSBSubscribeResponseMessage response = new TSBSubscribeResponseMessage();
            response.setCallBack(callback);
            send(msg, response);
        }
    }
}
