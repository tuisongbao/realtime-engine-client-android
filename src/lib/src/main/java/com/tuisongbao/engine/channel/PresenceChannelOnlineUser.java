package com.tuisongbao.engine.channel;

import com.google.gson.JsonObject;
import com.tuisongbao.engine.EngineOptions;

/**
 * {@link com.tuisongbao.engine.channel.PresenceChannel} 的当前在线用户实体
 *
 * <P>
 *     只用于 {@link com.tuisongbao.engine.channel.PresenceChannel}， 在订阅成功的回调处理方法中，会返回由该类组成的当前在线用户列表。
 *     该类的内容来源于创建 Engine 时指定的 {@link EngineOptions#getAuthEndpoint()}。
 *
 * @see com.tuisongbao.engine.channel.Channel#EVENT_SUBSCRIPTION_SUCCESS
 */
public class PresenceChannelOnlineUser {
    private String userId;
    private JsonObject userInfo;

    /**
     * 获取用户信息
     *
     * @return  用户信息
     */
    public JsonObject getUserInfo() {
        return userInfo;
    }

    public String getUserId() {
        return userId;
    }
}
