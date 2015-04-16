package com.tuisongbao.android.engine.channel.entity;

import org.json.JSONException;
import org.json.JSONObject;

import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.http.HttpConstants;
import com.tuisongbao.android.engine.http.request.BaseRequest;
import com.tuisongbao.android.engine.http.response.BaseResponse;
import com.tuisongbao.android.engine.util.ExecutorUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBPrivateChannel extends TSBChannel {
    public TSBPrivateChannel(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    private String signature;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    protected JSONObject getHttpRequestObjectOfAuth() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("socketId", TSBEngine.getSocketId());
        json.put("channelName", channel);

        return json;
    }

    protected boolean validateResponseDataOfAuth(JSONObject data, TSBEngineCallback<String> callback) {
        signature = data.optString("signature");
        if (StrUtil.isEmpty(signature)) {
            callback.onError(EngineConstants.CHANNEL_CODE_INVALID_OPERATION_ERROR, "Auth failed, Signature field is empty");
            return false;
        }
        callback.onSuccess("OK");
        return true;
    }

    @Override
    protected void validata(final TSBEngineCallback<String> callback) {
        ExecutorUtil.getThreadQueue().execute(new Runnable() {

            @Override
            public void run() {
                JSONObject json = null;
                try {
                    json = getHttpRequestObjectOfAuth();
                    callback.onError(EngineConstants.CHANNEL_CODE_INVALID_OPERATION_ERROR, "Error accured when call auth server");
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                @SuppressWarnings("null")
                BaseRequest authRequest = new BaseRequest(
                        HttpConstants.HTTP_METHOD_POST, TSBEngine.getTSBEngineOptions().getAuthEndpoint(), json.toString());
                BaseResponse authResponse = authRequest.execute();
                if (authResponse == null || !authResponse.isStatusOk()) {
                    callback.onError(EngineConstants.CHANNEL_CODE_INVALID_OPERATION_ERROR, "Error accured when call auth server");
                    return;
                }

                JSONObject jsonData = authResponse.getJSONData();
                if (jsonData == null) {
                    callback.onError(EngineConstants.CHANNEL_CODE_INVALID_OPERATION_ERROR, "Auth failed, auth server response nothing");
                    return;
                }
                validateResponseDataOfAuth(jsonData, callback);
            }
        });
    }

}
