package com.tuisongbao.android.engine.channel.entity;

import com.tuisongbao.android.engine.entity.TSBUserInfo;

public class TSBChannelUserData {

    private String userId;
    private TSBUserInfo userInfo;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public TSBUserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(TSBUserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
