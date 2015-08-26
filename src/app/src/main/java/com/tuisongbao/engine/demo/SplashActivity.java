package com.tuisongbao.engine.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.github.nkzawa.emitter.Emitter;
import com.tuisongbao.engine.connection.Connection;
import com.tuisongbao.engine.demo.app.App;
import com.tuisongbao.engine.demo.utils.AppToast;
import com.tuisongbao.engine.demo.utils.LogUtil;

public class SplashActivity extends Activity {

    protected static final String TAG = LogUtil.makeLogTag(SplashActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalParams.activity = this;

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//无标题
        setContentView(R.layout.cgt_activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏

        bindConnectionEvent();
    }

    private void bindConnectionEvent() {
        Connection connection = App.getContext().getEngine().getConnection();

        Emitter.Listener stateListener = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Connection.State state = (Connection.State)args[0];
                        String msg = "Connecting state: " + state.toString();
                        AppToast.getToast().show(msg);
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
                        Log.i(TAG, msg);
                        AppToast.getToast().show(msg);
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
                        Log.i(TAG, msg);
                        AppToast.getToast().show(msg);
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
                        Log.i(TAG, msg);
                        AppToast.getToast().show(msg);
                    }
                });
            }
        });

        if(connection.isConnected()){
            Intent intent = new Intent(SplashActivity.this, MainActivity_.class);
            startActivity(intent);
            finish();
        }else{
            connection.bindOnce(Connection.State.Connected.getName(), new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Intent intent = new Intent(SplashActivity.this, MainActivity_.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

    }
}
