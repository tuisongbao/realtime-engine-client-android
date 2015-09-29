package com.tuisongbao.engine.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(NotificationHandleService.ACTION_NEW_NOTIFICATION)) {
            String message = intent.getExtras().getString(NotificationHandleService.EXTRA_DATA);
            PackageManager pm = context.getPackageManager();
            Intent i = pm.getLaunchIntentForPackage(context.getPackageName());
            i.putExtra(NotificationHandleService.EXTRA_DATA, message);
            context.startActivity(i);
        }
    }
}
