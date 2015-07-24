package com.tuisongbao.engine.chat.entity;

import java.util.HashMap;

import com.tuisongbao.engine.chat.entity.TSBMessage.TYPE;

public class TSBChatUser {

    private boolean isNew;
    private String userId;
    private HashMap<String, String> uploadTokens = new HashMap<String, String>();

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /***
     *
     * @param forWhat image, voice or video
     * @return
     */
    public String getUploadToken(String forWhat) {
        return uploadTokens.get(forWhat);
    }

    @Override
    public String toString() {
        return String.format("TSBChatUser[isNew: %s, userId: %s, image uptoken: %s, voice uptoken: %s]"
                , isNew, userId, uploadTokens.get(TYPE.IMAGE.getName()), uploadTokens.get(TYPE.VOICE.getName()));
    }
}
