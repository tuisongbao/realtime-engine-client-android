package com.tuisongbao.engine.channel;

import com.github.nkzawa.emitter.Emitter;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.utils.StrUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <STRONG>Presence Channel</STRONG>
 *
 * <P>
 *     在 {@link PrivateChannel} 的基础上，可以监听用户上下线的通知，使用 {@link #bind(String, Emitter.Listener)} 方法可以获取以下事件的回调通知：
 *
 * <UL>
 *     <LI>{@code engine:user_added}</LI>
 *     <LI>{@code engine:user_removed}</LI>
 * </UL>
 */
public class PresenceChannel extends PrivateChannel {
    /**
     * 用户上线时会触发该事件，事件回调有一个参数，类型为 {@link PresenceChannelUser}；
     */
    public static final String EVENT_USER_ADDED = "engine:user_added";
    /**
     * 用户下线时会触发该事件，事件回调有一个参数，类型为 {@link PresenceChannelUser}；
     */
    public static final String EVENT_USER_REMOVED = "engine:user_removed";

    private String channelData;
    transient private String authData;

    public PresenceChannel(String name, Engine engine) {
        super(name, engine);
    }

    private void setChannelData(String channelData) {
        this.channelData = channelData;
    }

    public void setAuthData(String authData) {
        this.authData = authData;
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
        PresenceChannel data = new PresenceChannel(channel, engine);
        data.setSignature(signature);
        data.setChannelData(channelData);
        message.setData(data);

        return message;
    }

    @Override
    protected void validate(EngineCallback<String> callback) {
        if (StrUtils.isEmpty(authData)) {
            ResponseError error = new ResponseError();
            error.setMessage("AuthData is required when subscribe a presence channel");
            callback.onError(error);
            return;
        }
        super.validate(callback);
    }

    @Override
    protected boolean validateResponseDataOfAuth(JSONObject data,
            EngineCallback<String> callback) {
        boolean pass = super.validateResponseDataOfAuth(data, callback);
        if (!pass) {
            // If private validate failed, no need to validate presence data.
            return false;
        }
        channelData = data.optString("channelData");
        if (StrUtils.isEmpty(channelData)) {
            ResponseError error = new ResponseError();
            error.setMessage("Auth failed, channelData field is empty");
            callback.onError(error);
            return false;
        }
        callback.onSuccess("OK");
        return true;
    }
}
