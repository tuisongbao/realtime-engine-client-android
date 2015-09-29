package com.tuisongbao.engine.demo.common.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.chat.message.ChatMessage;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.common.utils.NetClient;
import com.tuisongbao.engine.demo.common.utils.Utils;
import com.tuisongbao.engine.demo.common.view.widght.dialog.FlippingLoadingDialog;
import com.tuisongbao.engine.demo.conversation.view.activity.ChatConversationActivity;
import com.tuisongbao.engine.demo.conversation.view.activity.ChatConversationActivity_;

import org.apache.http.message.BasicNameValuePair;

public class BaseActivity extends Activity {
    protected Activity context;
    protected NetClient netClient;
    protected FlippingLoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        // 为了关闭时关闭全部的activity
        App.getInstance().addActivity(this);
        netClient = new NetClient(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.activityResumed();
    }

    public void onPause() {
        super.onPause();
        App.activityPaused();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Utils.finish(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void gotoConversation(ChatMessage message) {
        Intent intent = new Intent(this, ChatConversationActivity_.class);
        String target = message.getRecipient();
        if (message.getChatType() == ChatType.SingleChat) {
            target = message.getFrom();
        }
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET, target);
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE, message.getChatType());

        startActivity(intent);
    }

    /**
     * 打开 Activity
     *
     * @param activity
     * @param cls
     * @param name
     */
    public void start_Activity(Activity activity, Class<?> cls,
                               BasicNameValuePair... name) {
        Utils.start_Activity(activity, cls, name);
    }

    /**
     * 关闭 Activity
     *
     * @param activity
     */
    public void finish(Activity activity) {
        Utils.finish(activity);
    }

    /**
     * 判断是否有网络连接
     */
    public boolean isNetworkAvailable(Context context) {
        return Utils.isNetworkAvailable(context);
    }

    public FlippingLoadingDialog getLoadingDialog(String msg) {
        if (mLoadingDialog == null){
            mLoadingDialog = new FlippingLoadingDialog(this, msg);
        }

        return mLoadingDialog;
    }
}
