package com.tuisongbao.engine.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.apkfuns.logutils.LogUtils;
import com.github.nkzawa.emitter.Emitter;
import com.tuisongbao.engine.connection.Connection;
import com.tuisongbao.engine.demo.account.view.activity.LoginActivity_;
import com.tuisongbao.engine.demo.common.utils.Utils;

import org.androidannotations.annotations.EActivity;

/**
 * Created by user on 15-8-31.
 */
@EActivity
public class SplashActivity extends Activity{
    private Context mContext;
    private Connection connection;

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
    }

    private void gotoLoginView(){
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