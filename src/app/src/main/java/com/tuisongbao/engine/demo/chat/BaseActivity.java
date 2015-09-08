package com.tuisongbao.engine.demo.chat;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.tuisongbao.engine.chat.message.ChatMessage;

/**
 * Created by user on 15-9-1.
 */
public class BaseActivity extends FragmentActivity {
    private static final int notifiId = 11;
    protected NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // onresume时，取消notification显示
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * 当应用在前台时，如果当前消息不是属于当前会话，在状态栏提示一下 如果不需要，注释掉即可
     *
     * @param message
     */
    protected void notifyNewMessage(ChatMessage message) {

    }

    /**
     * 返回
     *
     * @param view
     */
    public void back(View view) {
        finish();
    }

}
