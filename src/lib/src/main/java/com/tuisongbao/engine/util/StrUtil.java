package com.tuisongbao.engine.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;

import android.annotation.SuppressLint;

import com.tuisongbao.engine.log.LogUtil;

/**
 * General-purpose string utilities.
 */
public class StrUtil
{
    public static final String CODE_FORMAT1_CONNECTOR_1 = "&";
    public static final String CODE_FORMAT1_CONNECTOR_2 = "#";
    public static final String CODE_FORMAT1_CONNECTOR_3 = ",";
    public static final String URL_CONNECTOR = "#";

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

    /**
     * Convert value to long. If convert failed, it will return default value.
     *
     * @param value
     * @param dft
     * @return
     */
    public static long toLong(String value, long dft)
    {
        if (!isEmpty(value))
        {
            try
            {
                return Long.parseLong(value);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return dft;
    }

    /**
     * Convert value to int. If convert failed, it will return default value.
     * @param value
     * @param dft
     * @return
     */
    public static int toInt(Long value, int dft)
    {
        if (value != null)
        {
            try
            {
                return dft = Integer.parseInt(value.toString());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return dft;
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

    public static String invokeRegxReplace(String src, String regEx, String rep) {

        if (StrUtil.isEmpty(src)) {
            return "";
        }

        Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pat.matcher(src);
        if (matcher.find()) {
                return matcher.replaceAll(rep);
        }
        else {
                return src;
        }
    }


    public static String invokeRegxFindString(String src, String regEx) {

        if (StrUtil.isEmpty(src)) {
            return "";
        }
        Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pat.matcher(src);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return "";
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

    /**
     * L: letter N:number C:chinese
     * @param string
     * @return
     */
    public static boolean valideStringOnlyHasLNC(String string) {
        String regExString = "^[\\u4e00-\\u9fa5a-zA-Z0-9]+$";
        String result = StrUtil.invokeRegxFindString(string, regExString);
        return !StrUtil.isEmpty(result);
    }

    /**
     * L: letter N:number C:chinese C:comma (dou hao)
     * @param string
     * @return
     */
    public static boolean valideStringOnlyHasLNCC(String string) {
        String regExString = "^[\\u4e00-\\u9fa5a-zA-Z0-9,]+$";
        String result = StrUtil.invokeRegxFindString(string, regExString);
        LogUtil.debug(LogUtil.LOG_TAG, "string:" + string + "  result:" + result);
        return !StrUtil.isEmpty(result);
    }

    public static String getTimeStringIOS8061(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(date);
    }

    public static String getStringFromJSONArray(JSONArray array) {
        String result = "";
        try {
            for (int i = 0; i < array.length(); i++) {
                result += array.getString(i) + ",";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.substring(0, result.length() - 1);
    }

    public static JSONArray getJsonArrayFromString(String string) {
        String[] items = string.split(",");
        JSONArray array = new JSONArray();
        for (int i = 0; i < items.length; i++) {
            array.put(items[i]);
        }
        return array;
    }

    public static String codeKey(String a, String b) {
        return a + b;
    }

    public static String decodeB(String origin, String a) {
        return origin.substring(a.length());
    }

    public static String decodeA(String origin, String b) {
        return origin.substring(0, origin.length() - b.length());
    }

    @SuppressLint("SimpleDateFormat")
    public static String getUTCTimeString() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("utc"));
        return format.format(new Date());
    }

    public static String getTimestampStringOnlyContainNumber(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(date);
    }

    public static List<String> spliteString(String origin, String separator) {
        try {
            return Arrays.asList(origin.split("\\s*" + separator + "\\s*"));
        } catch (Exception e) {
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, "error with origin:" + origin + "   separator:" + separator, e);
        }
        return null;
    }

    public static String codeKey(String a, String b, String c) {
        return a + CODE_FORMAT1_CONNECTOR_1 + b + CODE_FORMAT1_CONNECTOR_2 + c;
    }

    public static String decodeA(String origin) {

        int index1 = origin.indexOf(CODE_FORMAT1_CONNECTOR_1);
        if (index1 >= 0) {
            return origin.substring(0, index1);
        }
        return "";
    }

    public static String decodeB(String origin) {

        int index1 = origin.indexOf(CODE_FORMAT1_CONNECTOR_1);
        int index2 = origin.indexOf(CODE_FORMAT1_CONNECTOR_2);

        if (index1 >= 0 && index2 >= 0) {
            return origin.substring(index1 + 1, index2);
        }
        return "";
    }

    public static String decodeC(String origin) {

        int index2 = origin.indexOf(CODE_FORMAT1_CONNECTOR_2);
        if (index2 >= 0) {
            return origin.substring(index2 + 1);
        }
        return "";
    }

    public static String link(List<String> paramList) {

        String resultString="";
        if (paramList == null || paramList.size() < 1) {
            return resultString;
        }
        resultString = paramList.get(0);

        for (int i = 1; i < paramList.size(); i++) {
            resultString = resultString + CODE_FORMAT1_CONNECTOR_1 + paramList.get(i);
        }
        return resultString;
    }
}