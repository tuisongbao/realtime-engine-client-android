package com.tuisongbao.android.engine;

import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.tuisongbao.android.engine.util.StrKeyUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public final class EnginePreference {
    private Context mContext = null; // this should be the application context
                                     // or the application context
    private static final String PRE_NAME = "com.tuisongbao.android.push.db";
    private static EnginePreference mInstance = null;

    /**
     * Default lifespan (30 days) of the {@link #isRegisteredOnServer(Context)}
     * flag until it is considered expired.
     */
    // NOTE: cannot use TimeUnit.DAYS because it's not available on API Level 8
    /**
     * 1000 * 3600 * 24 * 30;
     */
    public static final long DEFAULT_ON_SERVER_LIFESPAN_MS = 2592000000L;

    private static final String PROPERTY_ON_SERVER = "onServer";
    private static final String PROPERTY_NEW_USER = "newUser";
    private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTime";
    private static final String PROPERTY_ON_SERVER_LIFESPAN = "onServerLifeSpan";
    private static final String PROPERTY_USER_VARS = "userVars";
    private static final String PROPERTY_APP_VERSION = "app-version";
    private static final String PROPERTY_NOTIFICATION_NID = "notification-nid";
    private static final String PROPERTY_REGISTERATION_CHANCES = "registeration-chances";
    private static final String PROPERTY_LAST_LOCATION = "last-location";
    private static final String PROPERTY_FAILED_REQUEST = "failed-request";

    private static SharedPreferences db = null;

    // if this value is 3000, it most likely that this alarm is expired.
    public final int DEFAULT_BACKOFF_MS = 6000;  // 1 min

    private EnginePreference() {
        // empty here
    }

    public static synchronized EnginePreference instance() {

        if (null == mInstance) {
            mInstance = new EnginePreference();
//            db = PushManager.getApplicationContext().getSharedPreferences(
//                    PRE_NAME, 0);
        }
        return mInstance;
    }

    public void init(Context context) {

        mContext = context;
        db = mContext.getSharedPreferences(PRE_NAME, 0);
    }

    public String getAppVersion() {
        return db.getString(PROPERTY_APP_VERSION, "");
    }

    public void setAppVersion(String newAppVersion) {
        SharedPreferences.Editor editor = db.edit();
        editor.putString(PROPERTY_APP_VERSION, newAppVersion);
        editor.commit();
    }

    public int getCachedAppCode() {
        return db.getInt(StrKeyUtil.APP_CODE, Integer.MIN_VALUE);
    }

    public void setCachedAppCode(int code) {

        SharedPreferences.Editor editor = db.edit();
        editor.putInt(StrKeyUtil.APP_CODE, code);
        editor.commit();
    }

    public String getAppToken() {
        return db.getString(StrKeyUtil.APP_TOKEN, "");
    }

    public void setAppToken(String token, boolean fromGCM) {

        SharedPreferences.Editor editor = db.edit();
        editor.putString(StrKeyUtil.APP_TOKEN, token);
        editor.putBoolean(StrKeyUtil.APP_REG_TO_GCM, fromGCM);

        editor.commit();
    }

    public boolean isTokenFromGCM() {
        return db.getBoolean(StrKeyUtil.APP_REG_TO_GCM, false);
    }

    public String getUdid() {
        return db.getString(StrKeyUtil.DEVICE_UDID, "");
    }

    public void setUdid(String udid) {

        SharedPreferences.Editor editor = db.edit();
        editor.putString(StrKeyUtil.DEVICE_UDID, udid);
        editor.commit();
    }

    public int getGCMRequestBackoff() {
        return db.getInt(StrKeyUtil.GCM_BACK_OFF, DEFAULT_BACKOFF_MS);
    }

    public void setGCMRequestBackoff(int backoff) {
        SharedPreferences.Editor editor = db.edit();
        editor.putInt(StrKeyUtil.GCM_BACK_OFF, backoff);
        editor.commit();
    }

    public void resetGCMRequestBackoff() {
        this.setGCMRequestBackoff(DEFAULT_BACKOFF_MS);
    }

    public long getRegisterOnServerLifespan() {
        return db.getLong(PROPERTY_ON_SERVER_LIFESPAN,
                DEFAULT_ON_SERVER_LIFESPAN_MS);
    }

    public void setRegisterOnServerLifespan(long span) {

        SharedPreferences.Editor editor = db.edit();
        editor.putLong(PROPERTY_ON_SERVER_LIFESPAN, span);
        editor.commit();
    }

    public boolean tokenSyncedToServer() {
        return db.getBoolean(PROPERTY_ON_SERVER, false);
    }

    public void setTokenSyncedOnServer(boolean synced) {

        SharedPreferences.Editor editor = db.edit();
        editor.putBoolean(PROPERTY_ON_SERVER, synced);
        editor.commit();
    }

    public void setRegisterOnServerExpirationTime(long time) {

        SharedPreferences.Editor editor = db.edit();
        editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, time);
        editor.commit();
    }

    public long getRegisterOnServerExpirationTime() {
        return db.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
    }

    public void updateUserVars(JSONObject userVars) {

        try {
            JSONObject jsonObject = getUserVars();

            Iterator<String> keys = userVars.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = userVars.getString(key);
                if (StrUtil.isEmpty(value)) {
                        jsonObject.remove(key);
                } else {
                    jsonObject.put(key, userVars.get(key));
                }
            }
            SharedPreferences.Editor editor = db.edit();
            editor.putString(PROPERTY_USER_VARS, jsonObject.toString());
            editor.commit();

        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    public JSONObject getUserVars() {
        try {
            JSONObject object = new JSONObject(db.getString(PROPERTY_USER_VARS, "{}"));
            return object;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public void setNewUser(boolean isNew) {

        SharedPreferences.Editor editor = db.edit();
        editor.putBoolean(PROPERTY_NEW_USER, isNew);
        editor.commit();
    }

    public boolean getNewUser() {
        return db.getBoolean(PROPERTY_NEW_USER, false);
    }

    public void setLastNotificationNid(String nid) {

        SharedPreferences.Editor editor = db.edit();
        editor.putString(PROPERTY_NOTIFICATION_NID, nid);
        editor.commit();
    }

    public String getLastNotificationNid() {
        return db.getString(PROPERTY_NOTIFICATION_NID, "");
    }

    public int getRegisterationChances() {
        return db.getInt(PROPERTY_REGISTERATION_CHANCES, 0);
    }

    public void setRegisterationChances(int chance) {

        SharedPreferences.Editor editor = db.edit();
        editor.putInt(PROPERTY_REGISTERATION_CHANCES, chance);
        editor.commit();
    }

    public String getLastLocation() {
        return db.getString(PROPERTY_LAST_LOCATION, "");
    }

    public void setLastLocation(String locationJsonString) {

        SharedPreferences.Editor editor = db.edit();
        editor.putString(PROPERTY_LAST_LOCATION, locationJsonString);
        editor.commit();
    }

    public void cacheFailedRequest(List<String> paramList) {

        String usedRequetString = getFailedRequest();
        if (!StrUtil.isEmpty(usedRequetString)) {
            usedRequetString += StrUtil.URL_CONNECTOR;
        }

        usedRequetString += StrUtil.link(paramList);

        SharedPreferences.Editor editor = db.edit();
        editor.putString(PROPERTY_FAILED_REQUEST, usedRequetString);
        editor.commit();
    }

    public String getFailedRequest() {
        return db.getString(PROPERTY_FAILED_REQUEST, "");
    }

    public void clearTriggeredGeofenceRequest() {

        SharedPreferences.Editor editor = db.edit();
        editor.putString(PROPERTY_FAILED_REQUEST, "");
        editor.commit();
    }
}