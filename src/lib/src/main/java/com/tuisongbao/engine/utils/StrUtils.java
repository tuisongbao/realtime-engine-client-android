package com.tuisongbao.engine.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
     * 获取一个非 null 的 String，如果 str 为 null，返回空字符串，否则返回原 str
     *
     * @param str 字符串
     * @return 非 null 字符串
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

            return strRight.equals(strLeft);
        }
    }

    /**
     * 以指定的分隔符来进行字符串元素连接
     * <p>
     * 例如有字符串数组array和连接符为逗号(,)
     * <code>
     * String[] array = new String[] { "hello", "tuisongbao" };
     * </code>
     * 那么得到的结果是:
     * <code>
     * hello,tuisongbao
     * </code>
     * </p>
     *
     * @param array 需要连接的字符串数组
     * @param sep   元素连接之间的分隔符
     * @return 连接好的新字符串
     */
    public static String join(String[] array, String sep) {
        if (array == null) {
            return null;
        }

        int arraySize = array.length;
        int sepSize = 0;
        if (sep != null && !sep.equals("")) {
            sepSize = sep.length();
        }

        int bufSize = (arraySize == 0 ? 0 : ((array[0] == null ? 16 : array[0].length()) + sepSize) * arraySize);
        StringBuilder buf = new StringBuilder(bufSize);

        for (int i = 0; i < arraySize; i++) {
            if (i > 0) {
                buf.append(sep);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    /**
     * @see #join(String[], String)
     */
    public static String join(List<String> list, String sep) {
        if (list == null || list.size() < 1) {
            return null;
        }

        int arraySize = list.size();
        int sepSize = 0;
        if (sep != null && !sep.equals("")) {
            sepSize = sep.length();
        }

        int bufSize = (arraySize == 0 ? 0 : ((list.get(0) == null ? 16 : list.get(0).length()) + sepSize) * arraySize);
        StringBuilder buf = new StringBuilder(bufSize);

        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                buf.append(sep);
            }
            if (list.get(i) != null) {
                buf.append(list.get(i));
            }
        }
        return buf.toString();
    }

    public static String getTimeStringIOS8061(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(date);
    }

    public static Date getDateFromTimeStringIOS8061(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date();
        if (dateString != null) {
            try {
                date = format.parse(dateString);
            } catch (ParseException e) {
                LogUtils.error(TAG, e);
            }
        }
        return date;
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
