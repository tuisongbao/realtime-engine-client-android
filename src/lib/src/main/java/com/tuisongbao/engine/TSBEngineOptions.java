package com.tuisongbao.engine;

import com.tuisongbao.engine.service.TSBChatIntentService;

public class TSBEngineOptions {

    // For engin config
    private String mEngineAppId;

    // Auth endpoint
    private String mAuthEndPoint;
    // chat intent service
    private Class<? extends TSBChatIntentService> mChatIntentService = TSBChatIntentService.class;

    public TSBEngineOptions(String appId, String endpoint) {
        mEngineAppId = appId;
        mAuthEndPoint = endpoint;
    }

    public void setAuthEndpoint(String authEndpoint) {
        mAuthEndPoint = authEndpoint;
    }

    public String getAuthEndpoint()
    {
        return mAuthEndPoint;
    }

    public void setAppId(String appId) {
        mEngineAppId = appId;
    }

    public String getAppId()
    {
        return mEngineAppId;
    }

    public void setAppKey(String appKey) {
        mEngineAppId = appKey;
    }

    public Class<? extends TSBChatIntentService> getChatIntentService() {
        return mChatIntentService;
    }

    public void setChatIntentService(
            Class<? extends TSBChatIntentService> chatIntentService) {
        this.mChatIntentService = chatIntentService;
    }
}
