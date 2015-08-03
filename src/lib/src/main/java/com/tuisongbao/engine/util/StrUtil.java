package com.tuisongbao.engine.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

/**
 * General-purpose string utilities.
 */
public class StrUtil {
    private static final String TAG = StrUtil.class.getSimpleName();

    public static final String CODE_FORMAT1_CONNECTOR_1 = "&";
    public static final String CODE_FORMAT1_CONNECTOR_2 = "#";

    /**
     * Tests whether the given string is null or has zero length.
     *
     * @param str String to test.
     * @return true when the given string is null or zero length; false otherwise.
     */
    public static boolean isEmpty(String str)
    {
        return (str == null) || (str.length() == 0);
    }

    public static String creatUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * If str is null, returns "", or return str.
     *
     * @param str
     * @return
     */
    public static String strNotNull(String str) {
        return isEmpty(str) ? "" : str;
    }

    /**
     * Converts byte to hex string
     *
     * @param bytes
     * @return
     */
    public static String byte2hex(final byte[] bytes) {
        if (bytes == null || bytes.length < 0) {
            return "";
        }
        String hs = "";
        String stmp = "";
        for (int n = 0; n < bytes.length; n++) {
            stmp = (java.lang.Integer.toHexString(bytes[n] & 0xFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs;
    }

    public static boolean isEqual(String strRight, String strLeft) {
        if (StrUtil.isEmpty(strRight) && StrUtil.isEmpty(strLeft)) {
            return true;
        }
        else {
            if (StrUtil.isEmpty(strLeft)) {
                return false;
            }
            if (StrUtil.isEmpty(strRight)) {
                return false;
            }

            return strRight == strLeft || strRight.equals(strLeft);
        }
    }

    public static String getStringFromList(List<String> list) {

        String resultString = "";

        if (list != null && list.size() > 0) {
            resultString = list.get(0);
            for (int i = 1; i < list.size(); i++) {
                resultString += "," + list.get(i);
            }
        }
        return resultString;
    }

    public static String getTimeStringIOS8061(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(date);
    }

    public static String getTimestampStringOnlyContainNumber(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(date);
    }
}
