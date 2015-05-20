package com.tuisongbao.android.engine.demo.chat.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ToolUtils {

    public static String getDisplayTime(String timeString) {
        if (timeString == null) {
            return "";
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = format.parse(timeString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (isToday(date)) {
            format = new SimpleDateFormat("今天 HH:mm:ss");
        } else if (isYesterday(date)) {
            format = new SimpleDateFormat("昨天 HH:mm:ss");
        } else {
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        return format.format(date);
    }

    public static boolean isToday(Date date) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(new Date());
        cal2.setTime(date);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                          cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isYesterday(Date date) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(new Date());
        cal2.setTime(date);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                          (cal1.get(Calendar.DAY_OF_YEAR) - cal2.get(Calendar.DAY_OF_YEAR)) == 1;
    }
}
