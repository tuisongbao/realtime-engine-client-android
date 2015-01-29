package com.tuisongbao.android.engine.channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.tuisongbao.android.engine.EngineConfig;
import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.channel.entity.ChannelState;
import com.tuisongbao.android.engine.channel.entity.TSBChannel;
import com.tuisongbao.android.engine.channel.entity.TSBPresenceChannel;
import com.tuisongbao.android.engine.channel.message.TSBSubscribeMessage;
import com.tuisongbao.android.engine.channel.message.TSBUnsubscribeMessage;
import com.tuisongbao.android.engine.common.BaseManager;
import com.tuisongbao.android.engine.common.ITSBEngineCallback;
import com.tuisongbao.android.engine.common.TSBEngineBindCallback;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.connection.entity.TSBConnection;
import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.entity.TSBEngineConstants;
import com.tuisongbao.android.engine.http.HttpConstants;
import com.tuisongbao.android.engine.http.request.BaseRequest;
import com.tuisongbao.android.engine.http.response.BaseResponse;
import com.tuisongbao.android.engine.util.ExecutorUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBChannelManager extends BaseManager {

    Map<String, TSBSubscribeMessage> mChannelMap = new HashMap<String, TSBSubscribeMessage>();
    Map<String, ArrayList<TSBEngineBindCallback>> mBindMap = new HashMap<String, ArrayList<TSBEngineBindCallback>>();

    private static TSBChannelManager mInstance;

    public static TSBChannelManager getInstance() {
        if (mInstance == null) {
            mInstance = new TSBChannelManager();
        }
        return mInstance;
    }

    private TSBChannelManager() {
        bindConnectionEvents();
    }

    /**
     * 订阅channel
     * 
     * @param channel
     */
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
     * @param authData
     * @param callback
     */
    public void subscribe(String channel, String authData) {
        if (StrUtil.isEmpty(channel) || hasSubscribe(channel)) {
            return;
        }
        TSBSubscribeMessage msg = new TSBSubscribeMessage();
        TSBPresenceChannel data = new TSBPresenceChannel();
        data.setChannel(channel);
        msg.setAuthData(authData);
        msg.setData(data);
        bindChannelEvents(channel, msg);
        sendSubscribeMessage(msg);
    }

    /**
     * 取消订阅
     * 
     * @param channel
     * @param authData
     * @param callback
     */
    public void unSubscribe(String channel) {
        if (StrUtil.isEmpty(channel)) {
            return;
        }
        TSBSubscribeMessage msg = mChannelMap.get(channel);
        if (msg == null || msg.isUnsubscribeed()) {
            return;
        } else {
            if (msg.isSubscribeSending() && msg.isSubscribed()) {
                msg.setState(ChannelState.UNSUBSCRIBED);
                sendUnsubscribeMessage(msg);
            } else {
                removeChannel(channel);
            }
        }
    }

    public void bind(String bindName, TSBEngineBindCallback callback) {
        super.bind(bindName, callback);
    }

    public void unbind(String bindName, ITSBEngineCallback callback) {
        super.unbind(bindName, callback);
    }

    private void sendUnsubscribeMessage(TSBSubscribeMessage msg) {
        if (TSBEngine.isConnected()) {
            TSBUnsubscribeMessage unsubscribe = new TSBUnsubscribeMessage();
            TSBChannel channel = new TSBChannel();
            unsubscribe.setData(channel);
            send(unsubscribe);
        } else {
            removeChannel(msg.getData().getChannel());
        }
    }

    private void sendSubscribeMessage(TSBSubscribeMessage msg) {
        String channel = msg.getData().getChannel();
        if (isPrivateChannel(channel)) {
            msg.setState(ChannelState.SUBSCRIBE_SENDING);
            auth(msg);
        } else if (isPresenceChannel(channel)) {
            if (!StrUtil.isEmpty(msg.getAuthData())) {
                msg.setState(ChannelState.SUBSCRIBE_SENDING);
                auth(msg);
            } else {
                handleErrorMessage(
                        channel,
                        TSBEngineConstants.CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED_ERROR,
                        TSBEngineConstants.CHANNEL_CODE_INVALID_OPERATION_ERROR,
                        "auth data parameter can not be empty in presence channel");
                return;
            }
        } else if (TSBEngine.isConnected()) {
            msg.setState(ChannelState.SUBSCRIBE_SENDING);
            send(msg);
        } else {
            // when connected event triggered, sent it
            msg.setState(ChannelState.INITIAL);
        }
    }

    private boolean hasSubscribe(String channel) {
        TSBSubscribeMessage message = mChannelMap.get(channel);
        return message != null && !message.isUnsubscribeed();
    }

    private TSBSubscribeMessage removeChannel(String channel) {
        unbind(channel, mSubscribeEngineCallback);
        return mChannelMap.remove(channel);
    }

    private void bindConnectionEvents() {
        TSBEngine.connection.bind(TSBEngineConstants.TSBENGINE_BIND_NAME_CONNECTION_CONNECTED, mConnectionCallback);
    }
    
    private void handleErrorMessage(String channel, String name, int code, String message) {
        List<TSBEngineBindCallback> calls = mBindMap.get(channel);
        removeChannel(channel);
        if (calls != null && !calls.isEmpty()) {
            List<TSBEngineBindCallback> tempList = new ArrayList<TSBEngineBindCallback>(calls);
            String data = EngineConstants.genErrorJsonString(code, message);
            for (TSBEngineBindCallback callback : tempList) {
                callback.onEvent(channel, name, data);
            }
        }
    }

    private void bindChannelEvents(String channel, TSBSubscribeMessage msg) {
        bind(channel, mSubscribeEngineCallback);
        mChannelMap.put(channel, msg);
    }

    private void auth(final TSBSubscribeMessage msg) {
        ExecutorUtil.getThreadQueue().execute(new Runnable() {
            
            @Override
            public void run() {
                TSBPresenceChannel data = msg.getData();
                if (data == null) {
                    return;
                }
                String channel = data.getChannel();
                JSONObject json = new JSONObject();
                try {
                    json.put("socketId", TSBEngine.getSocketId());
                    json.put("channelName", channel);
                    if (isPresenceChannel(channel)) {
                        json.put("authData", msg.getAuthData());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                BaseRequest request = new BaseRequest(
                        HttpConstants.HTTP_METHOD_POST, EngineConfig.instance()
                                .getAuthEndpoint(), json.toString());
                BaseResponse response = request.execute();
                if (response != null && response.isStatusOk()) {
                    JSONObject jsonData = response.getJSONData();
                    if (jsonData == null) {
                        // feed back empty
                        handleErrorMessage(
                                channel,
                                TSBEngineConstants.CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED_ERROR,
                                TSBEngineConstants.CHANNEL_CODE_INVALID_OPERATION_ERROR,
                                "auth failed, feed back auth data is empty");
                    } else {
                        String signature = jsonData.optString("signature");
                        if (StrUtil.isEmpty(signature)) {
                            // signature data empty
                            handleErrorMessage(
                                    channel,
                                    TSBEngineConstants.CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED_ERROR,
                                    TSBEngineConstants.CHANNEL_CODE_INVALID_OPERATION_ERROR,
                                    "auth failed, signature is empty");
                        } else {
                            data.setSignature(signature);
                        }
                        if (isPresenceChannel(channel)) {
                            String channelData = jsonData.optString("channelData");
                            if (StrUtil.isEmpty(channelData)) {
                                // channel data empty
                                handleErrorMessage(
                                        channel,
                                        TSBEngineConstants.CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED_ERROR,
                                        TSBEngineConstants.CHANNEL_CODE_INVALID_OPERATION_ERROR,
                                        "auth failed, channel data is empty");
                            } else {
                                // send message
                                data.setChannelData(jsonData.toString());
                                send(msg);
                            }
                        } else {
                            // send message
                            send(msg);
                        }
                    }
                } else {
                    // connection to user server error or user server feed back error
                    handleErrorMessage(
                            channel,
                            TSBEngineConstants.CHANNEL_NAME_SUBSCRIPTION_SUCCEEDED_ERROR,
                            TSBEngineConstants.CHANNEL_CODE_INVALID_OPERATION_ERROR,
                            "auth failed, connection to user server error or user server feed back error");
                }
            }
        });
    }
    
    private boolean isPrivateChannel(String channel) {
        return channel
                .startsWith(TSBEngineConstants.TSBENGINE_CHANNEL_PREFIX_PRIVATE);
    }
    
    private boolean isPresenceChannel(String channel) {
        return channel
                .startsWith(TSBEngineConstants.TSBENGINE_CHANNEL_PREFIX_PRESENCE);
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
                        removeChannel(eventName);
                    }
                    if (EngineConstants.CHANNEL_NAME_UNSUBSCRIPTION_SUCCEEDED_ERROR
                            .equals(name)
                            || EngineConstants.CHANNEL_NAME_UNSUBSCRIPTION_SUCCEEDED
                                    .equals(name)) {
                        removeChannel(eventName);
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
            if (message.isUnsubscribeed()) {
                removeChannel(message.getName());
            } else {
                message.setState(ChannelState.SUBSCRIBE_SENDING);
                sendSubscribeMessage(message);
            }
        }
    }

    private void handeDisconnect() {
        HashSet<TSBSubscribeMessage> values = new HashSet<TSBSubscribeMessage>(mChannelMap.values());
        for (TSBSubscribeMessage message : values) {
            if (message.isUnsubscribeed()) {
                removeChannel(message.getName());
            }
        }
    }
}
