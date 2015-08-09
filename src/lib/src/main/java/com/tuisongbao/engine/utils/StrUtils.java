package com.tuisongbao.engine.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General-purpose string utilities.
 */
public class StrUtils {
    private static final String TAG = "TSB" + StrUtils.class.getSimpleName();

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

    /**
     * If str is null, returns "", or return str.
     *
     * @param str
     * @return
     */
    public static String strNotNull(String str) {
        return isEmpty(str) ? "" : str;
    }

    public static boolean isEqual(String strRight, String strLeft) {
        if (StrUtils.isEmpty(strRight) && StrUtils.isEmpty(strLeft)) {
            return true;
        }
        else {
            if (StrUtils.isEmpty(strLeft)) {
                return false;
            }
            if (StrUtils.isEmpty(strRight)) {
                return false;
            }

            return strRight == strLeft || strRight.equals(strLeft);
        }
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

    public static String invokeRegxReplace(String src, String regEx, String rep) {

        if (StrUtils.isEmpty(src)) {
            return "";
        }

        Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pat.matcher(src);
        if (matcher.find()) {
            return matcher.replaceFirst(rep);
        }
        else {
            return src;
        }
    }
}
