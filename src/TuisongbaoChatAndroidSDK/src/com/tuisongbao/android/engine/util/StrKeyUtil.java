package com.tuisongbao.android.engine.util;

public class StrKeyUtil {
    public static final String SERVICE_DB_APP_IDS = "service-db-app-keys";
    public static final String SERVICE_DB_TOKEN = "service-db-app-token";
    public static final String SERVICE_DB_MSG_NID = "service-db-msg-nid";
    public static final String SERVICE_DB_LOCATION = "service-db-location";
    public static final String SERVICE_DB_LOCATION_ENABLED = "service-db-location-enabled";

    //keys for gcm registration
    public static final String GCM_BACK_OFF = "backoff_ms";
    public static final String DEVICE_UDID = "app-udid";
    public static final String APP_TOKEN = "app-token";
    public static final String APP_REG_TO_GCM = "app-token-from-gcm";

    public static final String APP_CODE = "app-code";
    public static final String REMOVED_PACKAGE_NAME = "removed-package-name";

    public static final String OPERATION_HIT_MESSAGE = "operation-hit-message";
    public static final String OPERATION_HIT_MESSAGE_PARAM = "operation-hit-message-param";

    public static final String PUSH_RICH_MEDIA = "push-rich-media";

    public static final String PUSH_ERROR = "error";
    public static final String PUSH_ERROR_SERVICE_NOT_AVAILABLE = "SERVICE_NOT_AVAILABLE";
    public static final String PUSH_ERROR_SERVICE_NETWORK_ISSUE = "NETWORK_ISSUE";
    public static final String PUSH_ERROR_SERVICE_UN_HANDLE = "UN_HANDLE";

    // geofence
    public static final String PUSH_GEOFENCE = "push-geofence";
    public static final String PUSH_GEOFENCE_TRIGGER_TYPE = "push-geofence-trigger-type";
    public static final String PUSH_GEOFENCE_ID = "push-geofence-id";
    public static final String PUSH_STATUS_CODE = "push-status-code";
    public static final String PUSH_COUNT = "push-count";

    // location
    public static final String PUSH_LOCATION = "location";
    public static final String PUSH_DISTANCE = "distance";

    // the keys of stored geofence in PushPreference
    public static final String PUSH_LATITUDE = "lat";
    public static final String PUSH_LONGITUDE = "long";
    public static final String PUSH_RADIUS = "rad";
    public static final String PUSH_EXPIRATION_DURATION = "exp";
    public static final String PUSH_TIME = "push-time";
    public static final String PUSH_HAVE_TRIGGER_TYPE = "lastType";
    public static final String PUSH_TRIGGER_FREQUENCY = "frequency";
    public static final String PUSH_EXTRA= "extras";
}