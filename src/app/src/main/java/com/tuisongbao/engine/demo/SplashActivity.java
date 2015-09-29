package com.tuisongbao.engine.demo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.apkfuns.logutils.LogUtils;
import com.github.nkzawa.emitter.Emitter;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.chat.message.ChatMessage;
import com.tuisongbao.engine.connection.Connection;
import com.tuisongbao.engine.demo.account.view.activity.LoginActivity_;
import com.tuisongbao.engine.demo.common.utils.ChatMessageUtils;
import com.tuisongbao.engine.demo.common.utils.Utils;
import com.tuisongbao.engine.demo.common.view.activity.BaseActivity;
import com.tuisongbao.engine.demo.conversation.view.activity.ChatConversationActivity;
import com.tuisongbao.engine.demo.conversation.view.activity.ChatConversationActivity_;

import org.androidannotations.annotations.EActivity;

@EActivity
public class SplashActivity extends BaseActivity {
    private Context mContext;
    private Connection connection;

    private Emitter.Listener mMessageNewListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getInstance().addActivity(this);
        mContext = this;
        setContentView(R.layout.activity_start);
        bindConnectionEvent();

        if(connection.isConnected()){
            gotoLoginView();
        }else{
            connection.bindOnce(Connection.State.Connected.getName(), new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    gotoLoginView();
                }
            });
        }

        mMessageNewListener = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                if (App.isActivityVisible()) {
                    return;
                }
                ChatMessage message = (ChatMessage)args[0];
                String content = ChatMessageUtils.getMessageDigest(message, getApplicationContext());

                Context context = getApplicationContext();
                Intent intent = new Intent(context, ChatConversationActivity_.class);
                String target = message.getRecipient();
                if (message.getChatType() == ChatType.SingleChat) {
                    target = message.getFrom();
                }
                intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET, target);
                intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE, message.getChatType());

                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                Notification.Builder builder = new Notification.Builder(context);
                builder.setSmallIcon(App.getInstance().getApplicationInfo().icon)
                        .setContentText(message.getFrom() + ": " + content)
                        .setContentTitle(App.getInstance().getApplicationName())
                        .setContentIntent(pendingIntent)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true);
                Notification notification = builder.build();
                notificationManager.notify(1, notification);
            }
        };
        App.getInstance().getChatManager().bind(ChatManager.EVENT_MESSAGE_NEW, mMessageNewListener);
    }

    private void gotoLoginView() {
        Intent intent = new Intent();
        intent.setClass(SplashActivity.this, LoginActivity_.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
        finish();
    }

    private void bindConnectionEvent() {
        connection = App.getInstance().getEngine().getConnection();

        Emitter.Listener stateListener = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Connection.State state = (Connection.State)args[0];
                        String msg = "Connecting state: " + state.toString();
                        Utils.showShortToast(mContext, msg);
                    }
                });
            }
        };

        connection.bind(Connection.State.Initialized, stateListener);
        connection.bind(Connection.State.Connecting, stateListener);
        connection.bind(Connection.State.Connected, stateListener);
        connection.bind(Connection.State.Disconnected, stateListener);
        connection.bind(Connection.State.Failed, stateListener);

        connection.bind(Connection.EVENT_CONNECTING_IN, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = "Connecting in " + args[0] + " seconds";
                        Utils.showShortToast(mContext, msg);
                        LogUtils.i("EVENT_CONNECTING_IN", msg);
                    }
                });
            }
        });
        connection.bind(Connection.EVENT_STATE_CHANGED, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = "Connection state changed from " + args[0] + " to " + args[1];
                        Utils.showShortToast(mContext, msg);
                        LogUtils.i("EVENT_STATE_CHANGED", msg);
                    }
                });
            }
        });
        connection.bind(Connection.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg =  "Connection error," + args[0];
                        Utils.showShortToast(mContext, msg);
                        LogUtils.e("EVENT_ERROR", msg);
                    }
                });
            }
        });
    }
}
