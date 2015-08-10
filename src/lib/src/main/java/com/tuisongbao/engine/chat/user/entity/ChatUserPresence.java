package com.tuisongbao.engine.chat.user.entity;

/**
 * Created by root on 15-8-3.
 */
public class ChatUserPresence {
    private String userId;
    private String changedTo;

    public String getChangedTo() {
        return changedTo;
    }

    public String getUserId() {
        return userId;
    }
}
