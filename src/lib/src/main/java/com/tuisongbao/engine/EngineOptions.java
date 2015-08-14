package com.tuisongbao.engine;

/**
 * <STRONG>{@link Engine} 配置管理类</STRONG>
 *
 * <P>
 * 对 AppId 和 AuthEndPoint 的进行设置，在 Engine 初始化时会使用
 *
 * {@code new Engine(Context, EngineOptions)}
 *
 * @author Katherine Zhu
 */
public class EngineOptions {
    private final String mEngineAppId;
    private final String mAuthEndPoint;

    /**
     * 初始化配置
     *
     * @param appId     推送宝应用的唯一标识
     * @param endpoint  用户认证回调地址，可选，但建议启用
     */
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
