package com.tuisongbao.android.engine.channel.entity;

import com.tuisongbao.android.engine.entity.TBSUserInfo;

public class TSBChannelUserData {

    private String userId;
    private TBSUserInfo userInfo;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public TBSUserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(TBSUserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
