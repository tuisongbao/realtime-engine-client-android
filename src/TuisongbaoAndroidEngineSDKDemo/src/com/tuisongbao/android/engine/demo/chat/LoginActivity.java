package com.tuisongbao.android.engine.demo.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tuisongbao.android.engine.channel.TSBChannelManager;
import com.tuisongbao.android.engine.channel.entity.TSBChannel;
import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.entity.TSBChatUser;
import com.tuisongbao.android.engine.common.TSBEngineBindCallback;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.cache.LoginChache;
import com.tuisongbao.android.engine.util.StrUtil;

public class LoginActivity extends Activity {
    private static String TAG = "com.tuisongbao.android.engine.demo:LoginActivity";
    private EditText mEditTextAccount;
    private Button mButtonLogin;
    private Button mChannelButton;
    private Button mChannel2Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEditTextAccount = (EditText) findViewById(R.id.login_edittext_account);
        mButtonLogin = (Button) findViewById(R.id.login_textview_login);
        mButtonLogin.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (validate()) {
                    TSBChatManager.getInstance().login(mEditTextAccount.getText().toString(), new TSBEngineCallback<TSBChatUser>() {

                        @Override
                        public void onSuccess(TSBChatUser t) {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    LoginChache.setUserId(mEditTextAccount.getText().toString());
                                    Intent intent = new Intent(LoginActivity.this,
                                            DashboardActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }

                        @Override
                        public void onError(int code, String message) {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "登陆失败", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
            }
        });
        if (LoginChache.isLogin()) {
            Intent intent = new Intent(LoginActivity.this,
                    DashboardActivity.class);
            startActivity(intent);
            finish();
        }

        mChannel2Button = (Button) findViewById(R.id.login_channel2);
        mChannel2Button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                TSBChannel channel = TSBChannelManager.getInstance().subscribePrivateChannel("private-android-demo");
                channel.bind("engine:subscription_succeeded", new TSBEngineBindCallback() {

                    @Override
                    public void onEvent(String channelName, String eventName, String data) {
                        Log.e(TAG, "engine:subscription_succeeded");
                    }
                });

                channel.bind("engine:subscription_error", new TSBEngineBindCallback() {

                    @Override
                    public void onEvent(String channelName, String eventName, String data) {
                        Log.e(TAG, "engine:subscription_error");
                    }
                });

                channel.bind("cool-event", new TSBEngineBindCallback() {

                    @Override
                    public void onEvent(String channelName, String eventName, String data) {
                        Log.e(TAG, "cool-event: " + data);

                    }
                });
            }
        });
    }

    private boolean validate() {
        if (StrUtil.isEmpty(mEditTextAccount.getText().toString())) {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}
