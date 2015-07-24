package com.tuisongbao.engine.demo.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.tuisongbao.engine.chat.entity.TSBChatEvent;
import com.tuisongbao.engine.chat.entity.TSBEventMessageBody;
import com.tuisongbao.engine.chat.entity.TSBMessage;
import com.tuisongbao.engine.chat.entity.TSBMessage.TYPE;
import com.tuisongbao.engine.util.StrUtil;

public class ToolUtils {
    public static String getEventMessage(TSBMessage message) {
        if (message.getBody().getType() != TYPE.EVENT) {
            return "";
        }

        TSBEventMessageBody body = (TSBEventMessageBody)message.getBody();
        TSBChatEvent event = body.getEventType();
        String maker = message.getFrom();
        String target = body.getEventTarget();
        String eventMessage = "";
        if (event == TSBChatEvent.GroupJoined) {
            eventMessage = String.format("%s 邀请 %s 加入群组", maker, target);
        } else if (event == TSBChatEvent.GroupRemoved) {
            eventMessage = String.format("%s 被 %s 移出群组", target, maker);
        } else if (event == TSBChatEvent.GroupDismissed) {
            eventMessage = String.format("%s 解散了该群", maker);
        }
        return eventMessage;
    }

    public static String getDisplayTime(String timeString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date();
        if (timeString != null) {
            try {
                date = format.parse(timeString);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (isToday(date)) {
            format = new SimpleDateFormat("今天 HH:mm:ss");
        } else if (isYesterday(date)) {
            format = new SimpleDateFormat("昨天 HH:mm:ss");
        } else {
            format = new SimpleDateFormat("MM-dd HH:mm:ss");
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
}
