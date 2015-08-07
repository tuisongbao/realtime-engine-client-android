package com.tuisongbao.engine;

import com.tuisongbao.engine.chat.ChatIntentService;

public class EngineOptions {

    // For engin config
    private String mEngineAppId;

    // Auth endpoint
    private String mAuthEndPoint;
    // chat intent service
    private Class<? extends ChatIntentService> mChatIntentService = ChatIntentService.class;

    public EngineOptions(String appId, String endpoint) {
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

    public Class<? extends ChatIntentService> getChatIntentService() {
        return mChatIntentService;
    }

    public void setChatIntentService(
            Class<? extends ChatIntentService> chatIntentService) {
        this.mChatIntentService = chatIntentService;
    }
}
