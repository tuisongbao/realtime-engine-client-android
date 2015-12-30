package com.tuisongbao.engine.channel;

import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.http.HttpConstants;
import com.tuisongbao.engine.http.BaseRequest;
import com.tuisongbao.engine.http.BaseResponse;
import com.tuisongbao.engine.utils.LogUtils;
import com.tuisongbao.engine.utils.ExecutorUtils;
import com.tuisongbao.engine.utils.StrUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <STRONG>Private Channel</STRONG>
 *
 * <P>
 *     在普通 {@link Channel} 的基础上添加了用户认证的机制。
 */
public class PrivateChannel extends Channel {
    transient private static final String TAG = "TSB" + PrivateChannel.class.getSimpleName();

    String signature;

    public PrivateChannel(String name, Engine engine) {
        super(name, engine);
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    JSONObject getHttpRequestObjectOfAuth() throws JSONException {
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

    boolean validateResponseDataOfAuth(JSONObject data, EngineCallback<String> callback) {
        signature = data.optString("signature");
        if (StrUtils.isEmpty(signature)) {
            ResponseError error = new ResponseError();
            error.setMessage("Auth failed");
            callback.onError(error);
            return false;
        }
        return true;
    }

    @Override
    void validate(final EngineCallback<String> callback) {
        ExecutorUtils.getThreadQueue().execute(new Runnable() {

            @Override
            public void run() {
                JSONObject json;
                ResponseError error = new ResponseError();
                try {
                    json = getHttpRequestObjectOfAuth();
                } catch (JSONException e) {
                    callback.onError(engine.getUnhandledResponseError());
                    LogUtils.error(TAG, "Channel validation failed.", e);
                    return;
                }
                BaseRequest authRequest = new BaseRequest(
                        HttpConstants.HTTP_METHOD_POST, engine.getEngineOptions().getAuthEndpoint(), json.toString());
                BaseResponse authResponse = authRequest.execute();
                if (authResponse == null || !authResponse.isStatusOk()) {
                    error.setMessage("Error occurred when call auth server");
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
