package com.tuisongbao.android.engine.util;

import com.tuisongbao.android.engine.EngineConfig;

public class HttpParams {

    public static final String message = "message";
    public static final String ack = "ack";
    public static final String error = "error";
    public static final String tags = "tags";
    public static final String userVars = "userVars";
    public static final String apiKey = "apiKey";
    public static final String appKey = "appKey";
    public static final String appIds = "appIds";
    public static final String token = "token";
    public static final String regId = "regId";
    public static final String packageName = "packageName";
    public static final String appVersion = "appVersion";
    public static final String udid = "udid";
    public static final String messageNid = "messageNid";
    public static final String sign = "sign";
    public static final String unavaiAddrs = "unavaiAddrs";
    public static final String addrs = "addrs";
    public static final String body = "body";
    public static final String content = "content";
    public static final String registration_id = "registration_id";
    public static final String com_pushyun_nid = "com_pushyun_nid";
    public static final String com_pushyun_silence = "com_pushyun_silence";
    public static final String service = "service";

    public static final String osName = "osName";
    public static final String osNameValue = "Android";

    public static final String deviceName = "deviceName";
    public static final String deviceNameDefaultValue = android.os.Build.MODEL;
    public static final String deviceNameXiaoMi = "xiaomi";

    public static final String username_connector = "andriodsdk1.0";
    public static final String password_connector = "7ecfc6e6c4a8ab28fcf3a41d241c3888";
    public static final String username_location = "androidsdk2.0";
    public static final String password_location = "eda53f0311da25a0f11f2a8d";

    public static final String sdkVersion = "sdkVersion";
    public static final String sdkVersionValue = "2.2.0";

    // logcat
    public static final String file = "file";
    public static final String fileName = "fileName";
    public static final String fileMD5 = "fileMD5";

    // add when new user.
    public static final String newuser = "newuser";
    public static final String newuser_value_true = "1";
    public static final String newuser_value_false = "0";
    public static final String isNew = "isNew";

    // location
    public static final String locations = "locations";
    public static final String lat = "lat";
    public static final String lng = "lng";
    public static final String at = "at";

    // geofence params
    public static final String geoFences = "geoFences";
    public static final String circle = "circle";
    public static final String center = "center";
    public static final String radius = "radius";
    public static final String name = "name";
    public static final String id = "id";
    public static final String id_str = "id_str";
    public static final String fenceId = "fenceId";
    public static final String fenceIdStr = "fenceIdStr";
    public static final String triggerTime = "triggerTime";
    public static final String type = "type";
    public static final String trigger = "trigger";
    public static final String frequency = "frequency";
    public static final String event = "event";
    public static final String location = "location";
    public static final String geoFenceId = "geoFenceId";

    public static final String getServerUrl() {
        return EngineConfig.instance().getServerUrl() + "/v2/sdk";
    }

    public static final String getDevicesUrl() {
        return getServerUrl() + "/devices";
    }

    public static final String getConnectorsUrl() {
        return getServerUrl() + "/tpsconnectors";
    }

    public static final String getMessageClickUrl() {
        return getServerUrl() + "/stats/messageclicks";
    }

    public static final String getGeofencesUrl() {
        return getServerUrl() + "/geoFenceTriggers";
    }

    public static String getTagsUrl(String token) {
        return getDevicesUrl() + "/" + token + "/tags/bulk";
    }

    public static String getUserVarUrl(String token) {
        return getDevicesUrl() + "/" + token + "/uservars";
    }

    public static String getLocationUrl(String token) {
        return getDevicesUrl() + "/" + token + "/locations/bulk";
    }

    public static final String generateGeoFenceEventUrl(String geofenceId) {
        return getGeofencesUrl() + "/" + geofenceId + "/events";
    }
}