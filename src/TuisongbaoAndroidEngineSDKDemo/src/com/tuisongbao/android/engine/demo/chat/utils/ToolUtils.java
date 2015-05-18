package com.tuisongbao.android.engine.demo.chat.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ToolUtils {

    public static String getDisplayTime(String timeString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = format.parse(timeString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Date today = new Date();
        long delta = today.getTime() - date.getTime();
        if (delta < 24 * 60 * 60 * 1000) {
            format = new SimpleDateFormat("今天 HH:mm:ss");
        } else if (delta < 48 * 60 * 60 * 1000) {
            format = new SimpleDateFormat("昨天 HH:mm:ss");
        } else {
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        return format.format(date);
    }
}
