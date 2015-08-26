package com.tuisongbao.engine.channel;

/**
 * {@link com.tuisongbao.engine.channel.PresenceChannel} 中描述用户上下线的实体类
 *
 * @see com.tuisongbao.engine.channel.PresenceChannel#EVENT_USER_ADDED
 * @see com.tuisongbao.engine.channel.PresenceChannel#EVENT_USER_REMOVED
 */
public class PresenceChannelUser {
    private String id;
    private String info;

    public String getId() {
        return id;
    }

    public String getInfo() {
        return info;
    }
}
