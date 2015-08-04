package com.tuisongbao.engine.channel.entity;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.channel.message.SubscribeEvent;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.common.entity.ResponseEvent;
import com.tuisongbao.engine.http.HttpConstants;
import com.tuisongbao.engine.http.request.BaseRequest;
import com.tuisongbao.engine.http.response.BaseResponse;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.ExecutorUtil;
import com.tuisongbao.engine.util.StrUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class PrivateChannel extends Channel {
    private static final String TAG = "TSB" + PrivateChannel.class.getSimpleName();

    protected String signature;

    public PrivateChannel(String name, TSBEngine engine) {
        super(name, engine);
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    protected JSONObject getHttpRequestObjectOfAuth() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("socketId", engine.getConnection().getSocketId());
        json.put("channelName", channel);

        return json;
    }

    @Override
    protected SubscribeEvent generateSubscribeMessage() {
        SubscribeEvent message = new SubscribeEvent();
        PresenceChannel data = new PresenceChannel(channel, engine);
        data.setSignature(signature);
        message.setData(data);

        return message;
    }

    protected boolean validateResponseDataOfAuth(JSONObject data, TSBEngineCallback<String> callback) {
        signature = data.optString("signature");
        if (StrUtil.isEmpty(signature)) {
            ResponseError error = new ResponseError();
            error.setMessage("Auth failed");
            callback.onError(error);
            return false;
        }
        return true;
    }

    @Override
    protected void validate(final TSBEngineCallback<String> callback) {
        ExecutorUtil.getThreadQueue().execute(new Runnable() {

            @Override
            public void run() {
                JSONObject json;
                ResponseError error = new ResponseError();
                try {
                    json = getHttpRequestObjectOfAuth();
                } catch (JSONException e) {
                    callback.onError(engine.getUnhandledResponseError());
                    LogUtil.error(TAG, "Channel validation failed.",  e);
                    return;
                }
                BaseRequest authRequest = new BaseRequest(
                        HttpConstants.HTTP_METHOD_POST, engine.getEngineOptions().getAuthEndpoint(), json.toString());
                BaseResponse authResponse = authRequest.execute();
                if (authResponse == null || !authResponse.isStatusOk()) {
                    error.setMessage("Error accured when call auth server");
                    callback.onError(error);
                    return;
                }

                JSONObject jsonData = authResponse.getJSONData();
                if (jsonData == null) {
                    error.setMessage("Auth failed, auth server response nothing");
                    callback.onError(error);
                    return;
                }
                if (validateResponseDataOfAuth(jsonData, callback)) {
                    callback.onSuccess(channel);
                }
            }
        });
    }

}
