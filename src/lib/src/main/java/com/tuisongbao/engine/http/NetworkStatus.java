package com.tuisongbao.engine.http;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.StrUtil;

public class NetworkStatus
{
    public static final String WIFI = "WIFI";

    public static boolean isConnectedAsWifi(Context context) {
        return StrUtil.isEqual(WIFI, NetworkStatus.typeName(context));
    }

    private static ConnectivityManager connMan(Context context)
    {
        return (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private static NetworkInfo info(Context context)
    {
        ConnectivityManager localConnectivityManager = connMan(context);
        if (localConnectivityManager != null) return localConnectivityManager.getActiveNetworkInfo();
        LogUtil.error(LogUtil.LOG_TAG_HTTP, "Error fetching network info.");
        return null;
    }

    public static boolean isConnected(Context context)
    {
        NetworkInfo localNetworkInfo = info(context);
        if (localNetworkInfo == null) return false;
        return localNetworkInfo.isConnected();
    }

    public static String typeName(Context context)
    {
        NetworkInfo localNetworkInfo = info(context);
        if (localNetworkInfo == null) return "none";
        return localNetworkInfo.getTypeName();
    }

    public static String getActiveIPAddress(Context context)
    {
        String str = null;
        try
        {
            Enumeration<?> localEnumeration1 = null;
            for (int i = 0; (localEnumeration1 == null) && (i < 2); i++)
                try
                {
                    localEnumeration1 = NetworkInterface.getNetworkInterfaces();
                }
                catch (NullPointerException localNullPointerException)
                {
                    LogUtil.debug(LogUtil.LOG_TAG_HTTP, "NetworkInterface.getNetworkInterfaces failed with exception (ICS Bug): "
                            + localNullPointerException.toString());
                }
            if (localEnumeration1 == null)
            {
                LogUtil.debug(LogUtil.LOG_TAG_HTTP, "No network interfaces currently available.");
                return null;
            }
            while (localEnumeration1.hasMoreElements())
            {
                NetworkInterface localNetworkInterface = (NetworkInterface)localEnumeration1.nextElement();
                Enumeration<?> localEnumeration2 = localNetworkInterface.getInetAddresses();
                while (localEnumeration2.hasMoreElements())
                {
                    InetAddress localInetAddress = (InetAddress)localEnumeration2.nextElement();
                    if ((!localInetAddress.isLoopbackAddress()) && (str == null)) str = localInetAddress.getHostAddress();
                }
            }
        }
        catch (SocketException localSocketException)
        {
            LogUtil.error(LogUtil.LOG_TAG_HTTP, "Error fetching IP address information", localSocketException);
        }

        LogUtil.verbose(LogUtil.LOG_TAG_HTTP, "Detected active IP address as: " + str);
        return str;
    }
}