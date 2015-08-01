package com.tuisongbao.engine.channel.entity;

import org.json.JSONException;
import org.json.JSONObject;

import com.tuisongbao.engine.channel.TSBChannelManager;
import com.tuisongbao.engine.channel.message.SubscribeEvent;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.util.StrUtil;

public class TSBPresenceChannel extends TSBPrivateChannel {
    private String channelData;
    private String authData;

    public TSBPresenceChannel(String name, TSBChannelManager channelManager) {
        super(name, channelManager);
    }

    public String getChannelData() {
        return channelData;
    }

    public void setChannelData(String channelData) {
        this.channelData = channelData;
    }

    public void setAuthData(String authData) {
        this.authData = authData;
    }

    public String getAuthData() {
        return authData;
    }

    @Override
    protected JSONObject getHttpRequestObjectOfAuth() throws JSONException {
        JSONObject object = super.getHttpRequestObjectOfAuth();
        object.put("authData", authData);

        return object;
    }

    @Override
    protected SubscribeEvent generateSubscribeMessage() {
        SubscribeEvent message = new SubscribeEvent();
        TSBPresenceChannel data = new TSBPresenceChannel(channel, mChannelManager);
        data.setSignature(signature);
        data.setChannelData(channelData);
        message.setAuthData(authData);
        message.setData(data);

        return message;
    }

    @Override
    protected void validate(TSBEngineCallback<String> callback) {
        if (StrUtil.isEmpty(authData)) {
            callback.onError(Protocol.CHANNEL_CODE_INVALID_OPERATION_ERROR, "AuthData is required when subscribe a presence channel");
            return;
        }
        super.validate(callback);
    }

    @Override
    protected boolean validateResponseDataOfAuth(JSONObject data,
            TSBEngineCallback<String> callback) {
        boolean pass = super.validateResponseDataOfAuth(data, callback);
        if (!pass) {
            // If private validate failed, no need to validate presence data.
            return false;
        }
        channelData = data.optString("channelData");
        if (StrUtil.isEmpty(channelData)) {
            callback.onError(Protocol.CHANNEL_CODE_INVALID_OPERATION_ERROR, "Auth failed, channelData field is empty");
            return false;
        }
        callback.onSuccess("OK");
        return true;
    }
}
