package com.tuisongbao.engine.demo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.tuisongbao.push.service.NotificationIntentService;

import java.util.HashMap;

public class NotificationHandleService extends NotificationIntentService {
    public static final String ACTION_NEW_NOTIFICATION = "engine.demo.notification.new";
    public static final String EXTRA_DATA = "engine.demo.notification.new.data";

    @Override
    protected boolean silentNotification() {
        return true;
    }

    @Override
    protected void onMessageDelivered(HashMap<String, String> msgHashMap) {
        super.onMessageDelivered(msgHashMap);

        Notification notification = super.notification;
        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(getApplicationContext(), MyBroadcastReceiver.class);
        intent.setAction(ACTION_NEW_NOTIFICATION);
        intent.putExtra(EXTRA_DATA, msgHashMap.get("chatMessage"));
        notification.contentIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationManager.notify(1, this.notification);
    }
}
