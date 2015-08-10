package com.tuisongbao.engine;

/**
 * {@link Engine} 的配置选项，主要是 AppId 和 AuthEndPoint 的设置，在 Engine 初始化时会使用,
 *
 * {@code new Engine(Context, EngineOptions)}
 */
public class EngineOptions {

    // For engine config
    private String mEngineAppId;

    // Auth endpoint
    private String mAuthEndPoint;

    public EngineOptions(String appId, String endpoint) {
        mEngineAppId = appId;
        mAuthEndPoint = endpoint;
    }

    public String getAuthEndpoint()
    {
        return mAuthEndPoint;
    }

    public String getAppId()
    {
        return mEngineAppId;
    }
}
