package com.tuisongbao.android.engine.channel;

import com.tuisongbao.android.engine.TSBEngine.TSBEngineListener;
import com.tuisongbao.android.engine.channel.entity.TSBChannelSubscribeData;
import com.tuisongbao.android.engine.channel.entity.TSBChannelUserData;
import com.tuisongbao.android.engine.channel.message.TSBSubscribeMessage;
import com.tuisongbao.android.engine.common.BaseManager;

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

    public void subscribe(String channel, TSBEngineListener l) {
        subscribe(channel, null, null, l);
    }

    public void subscribe(String channel) {
        subscribe(channel, null, null, null);
    }

    public void subscribe(String channel, String signature,
            TSBChannelUserData userData, TSBEngineListener l) {
        TSBSubscribeMessage msg = new TSBSubscribeMessage();
        TSBChannelSubscribeData data = new TSBChannelSubscribeData();
        data.setChannel(channel);
        data.setChannelData(userData);
        data.setSignature(signature);
        msg.setData(data);
        send(msg, l);
    }
}
