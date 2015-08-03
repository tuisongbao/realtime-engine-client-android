package com.tuisongbao.engine.channel.entity;

import com.tuisongbao.engine.channel.ChannelManager;
import com.tuisongbao.engine.channel.message.SubscribeEvent;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.http.HttpConstants;
import com.tuisongbao.engine.http.request.BaseRequest;
import com.tuisongbao.engine.http.response.BaseResponse;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.ExecutorUtil;
import com.tuisongbao.engine.util.StrUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class PrivateChannel extends Channel {
    private static final String TAG = PrivateChannel.class.getSimpleName();

    protected String signature;

    public PrivateChannel(String name, ChannelManager channelManager) {
        super(name, channelManager);
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    protected JSONObject getHttpRequestObjectOfAuth() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("socketId", mChannelManager.engine.connection.getSocketId());
        json.put("channelName", channel);

        return json;
    }

    @Override
    protected SubscribeEvent generateSubscribeMessage() {
        SubscribeEvent message = new SubscribeEvent();
        PresenceChannel data = new PresenceChannel(channel, mChannelManager);
        data.setSignature(signature);
        message.setData(data);

        return message;
    }

    protected boolean validateResponseDataOfAuth(JSONObject data, TSBEngineCallback<String> callback) {
        signature = data.optString("signature");
        if (StrUtil.isEmpty(signature)) {
            callback.onError(Protocol.CHANNEL_CODE_INVALID_OPERATION_ERROR, "Auth failed, Signature field is empty");
            return false;
        }
        return true;
    }

    @Override
    protected void validate(final TSBEngineCallback<String> callback) {
        ExecutorUtil.getThreadQueue().execute(new Runnable() {

            @Override
            public void run() {
                JSONObject json = null;
                try {
                    json = getHttpRequestObjectOfAuth();
                } catch (JSONException e) {
                    callback.onError(Protocol.CHANNEL_CODE_INVALID_OPERATION_ERROR, "Tuisongbao internal error");
                    LogUtil.error(TAG, "Channel validation failed.",  e);
                    return;
                }
                BaseRequest authRequest = new BaseRequest(
                        HttpConstants.HTTP_METHOD_POST, mChannelManager.engine.getEngineOptions().getAuthEndpoint(), json.toString());
                BaseResponse authResponse = authRequest.execute();
                if (authResponse == null || !authResponse.isStatusOk()) {
                    callback.onError(Protocol.CHANNEL_CODE_INVALID_OPERATION_ERROR, "Error accured when call auth server");
                    return;
                }

                JSONObject jsonData = authResponse.getJSONData();
                if (jsonData == null) {
                    callback.onError(Protocol.CHANNEL_CODE_INVALID_OPERATION_ERROR, "Auth failed, auth server response nothing");
                    return;
                }
                if (validateResponseDataOfAuth(jsonData, callback)) {
                    callback.onSuccess(channel);
                }
            }
        });
    }

}
