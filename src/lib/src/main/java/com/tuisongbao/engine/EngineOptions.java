package com.tuisongbao.engine;

public class EngineOptions {

    // For engin config
    private String mEngineAppId;

    // Auth endpoint
    private String mAuthEndPoint;

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
}
