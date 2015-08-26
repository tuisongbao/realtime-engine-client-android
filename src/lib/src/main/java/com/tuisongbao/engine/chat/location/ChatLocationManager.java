package com.tuisongbao.engine.chat.location;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;

import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.common.BaseManager;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.utils.LogUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <STRONG>地理位置帮助类</STRONG>
 *
 * <P>
 *     获取当前位置，以经纬度标记。
 * </P>
 */
public class ChatLocationManager extends BaseManager {
    private static final String TAG = "TSB" + ChatLocationManager.class.getSimpleName();

    /**
     * 两个地理位置的时间间隔，以此判断是新的还是旧的地理位置。
     */
    private static final int TIME_DEVIATION = 15 * 1000;
    /**
     * 地理位置的最大精确度，用来判断地理位置的优劣，单位是 “米”。
     * 表示在以该地理位置的经纬度为中心，精确度为半径的圆中，有 68% 的可能，唯一正确的地理位置在该圆中。
     * 因此该值越大，地理位置越不精确，反之，越精确。
     */
    private static final int MIN_DISTANCE = 200;

    private static ChatLocationManager mInstance;
    private LocationManager manager;
    private Location bestLocation;

    /**
     * 超过该间隔，直接回调获取地理位置的处理方法。
     */
    private int TIMEOUT = 5 * 1000;
    /**
     * 获取地理位置超时处理方法
     */
    private Handler timeoutHandler;
    private Runnable callbackRunnable;

    ChatLocationManager() {
    }

    public static ChatLocationManager getInstance() {
        if (mInstance == null) {
            mInstance = new ChatLocationManager();
        }
        return mInstance;
    }

    /**
     * 获取当前的位置
     *
     * <P>
     *     处理方法的回调参数不保证一定会返回当前位置。在没有开启位置服务并且没有网络连接的情况下，是无法实时获取到当前位置的，此时会返回上一次记录的位置。
     *     此外，超过给定时间限制后会触发 {@code onSuccess} 的回调，并将上一次记录的位置返回。
     * </P>
     *
     * @param callback          处理方法
     * @param timeoutInSeconds  超时时间，单位是 “秒”，默认为 5 秒。
     * @return                  上一次记录的位置，可能为 {@code null}
     */
    public Location getCurrentLocation(final EngineCallback<Location> callback, int timeoutInSeconds) {
        try {
            int timeout = TIMEOUT;
            if (timeoutInSeconds > 0) {
                timeout = timeoutInSeconds * 1000;
            }
            manager = (LocationManager) Engine.getContext().getSystemService(Context.LOCATION_SERVICE);

            final LocationListener listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    LogUtils.info(TAG, "Get location update:" + locationDescription(bestLocation));
                    if (isBetterLocation(location, bestLocation)) {
                        bestLocation = location;
                        LogUtils.info(TAG, "Current best location:" + locationDescription(bestLocation));
                    }
                    timeoutHandler.removeCallbacks(callbackRunnable);
                    callbackRunnable.run();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    LogUtils.debug(TAG, "onStatusChanged " + provider + " " + status);
                }

                @Override
                public void onProviderEnabled(String provider) {
                    LogUtils.debug(TAG, "onProviderEnabled " + provider);
                }

                @Override
                public void onProviderDisabled(String provider) {
                    LogUtils.debug(TAG, "onProviderDisabled " + provider);
                }
            };

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String bestProvider = manager.getBestProvider(criteria, true);
            if (bestProvider == null) {
                manager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, null);
            } else {
                manager.requestSingleUpdate(bestProvider, listener, null);
            }

            for (String provider: manager.getAllProviders()) {
                Location location = manager.getLastKnownLocation(provider);
                LogUtils.debug(TAG, "Last known location:" + locationDescription(bestLocation));
                if (isBetterLocation(location, bestLocation)) {
                    bestLocation = location;
                    LogUtils.info(TAG, "Current best location:" + locationDescription(bestLocation));
                }
            }

            timeoutHandler = new Handler();
            callbackRunnable = new Runnable() {
                @Override
                public void run() {
                    LogUtils.info(TAG, "Time is up, callback with last best location");
                    manager.removeUpdates(listener);
                    if (bestLocation == null) {
                        ResponseError error = new ResponseError();
                        error.setMessage("Get location failed, please check the location access authority");
                        callback.onError(error);
                    } else {
                        callback.onSuccess(bestLocation);
                    }
                }
            };
            timeoutHandler.postDelayed(callbackRunnable, timeout);
            return bestLocation;
        } catch (Exception e) {
            callback.onError(engine.getUnhandledResponseError());
            LogUtils.error(TAG, e);
        }
        return null;
    }

    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (null == currentBestLocation) {
            return true;
        }

        if (location == null) {
            return false;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();

        boolean isSignificantlyNewer = timeDelta > TIME_DEVIATION;
        boolean isSignificantlyOlder = timeDelta < -TIME_DEVIATION;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // if the time span is between -TIME_DEVIATION and TIME_DEVIATION minutes
        // Check whether the new location fix is more or less accurate the radius more smaller more accurate.
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
                .getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > MIN_DISTANCE;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            // no matter new or old, use the more accurate one.
            return true;

        } else if (isNewer && !isLessAccurate) {
            // the two location's accuracy radius is same, use the new one.
            return true;

        } else if (isNewer && !isSignificantlyLessAccurate
                && isFromSameProvider) {
            // from the same provider, use new one only if the radius is less accuracy than 200 meters.
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private String locationDescription(Location location) {
        if (null == location) {
            return "null";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return "Time: " + format.format(new Date(location.getTime()))
                + " Provider: " + location.getProvider() + " ("
                + location.getLongitude() + "," + location.getLatitude() + ")";
    }
}
