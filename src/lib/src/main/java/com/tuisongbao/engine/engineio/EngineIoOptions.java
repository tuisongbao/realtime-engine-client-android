package com.tuisongbao.engine.engineio;

public class EngineIoOptions {

    private String mAppId;
    private String mPlatform;
    private String mProtocol = "v1";
    private String mSDKVersion = "v1.0.0";
    private String mTransport = "websocket";

    public String getAppId() {
        return mAppId;
    }

    public void setAppId(String appId) {
        this.mAppId = appId;
    }

    public String getPlatform() {
        return mPlatform;
    }

    public void setPlatform(String platform) {
        this.mPlatform = platform;
    }

    public String getProtocol() {
        return mProtocol;
    }

    public void setProtocol(String protocol) {
        this.mProtocol = protocol;
    }

    public String getSDKVersion() {
        return mSDKVersion;
    }

    public void setSDKVersion(String sdkVersion) {
        this.mSDKVersion = sdkVersion;
    }

    public String getTransport() {
        return mTransport;
    }

    public void setTransport(String transport) {
        this.mTransport = transport;
    }
}
