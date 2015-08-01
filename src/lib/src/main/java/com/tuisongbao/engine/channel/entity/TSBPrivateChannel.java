package com.tuisongbao.engine.channel.entity;

import org.json.JSONException;
import org.json.JSONObject;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.channel.TSBChannelManager;
import com.tuisongbao.engine.channel.message.TSBSubscribeMessage;
import com.tuisongbao.engine.common.TSBEngineCallback;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.http.HttpConstants;
import com.tuisongbao.engine.http.request.BaseRequest;
import com.tuisongbao.engine.http.response.BaseResponse;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.ExecutorUtil;
import com.tuisongbao.engine.util.StrUtil;

public class TSBPrivateChannel extends TSBChannel {
    public TSBPrivateChannel(String name, TSBChannelManager channelManager) {
        super(name, channelManager);
    }

    protected String signature;

    public String getSignature() {
        return signature;
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
    protected TSBSubscribeMessage generateSubscribeMessage() {
        TSBSubscribeMessage message = new TSBSubscribeMessage();
        TSBPresenceChannel data = new TSBPresenceChannel(channel, mChannelManager);
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
                    LogUtil.error(LogUtil.LOG_TAG_CHANNEL, "Channel validation failed.",  e);
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
