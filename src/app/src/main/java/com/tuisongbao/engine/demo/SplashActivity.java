package com.tuisongbao.engine.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.group.ChatGroup;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.connection.Connection;
import com.tuisongbao.engine.demo.bean.DemoGroup;
import com.tuisongbao.engine.demo.common.Utils;
import com.tuisongbao.engine.demo.service.ChatDemoService;
import com.tuisongbao.engine.demo.view.activity.LoginActivity_;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.rest.RestService;
import org.springframework.util.LinkedMultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by user on 15-8-31.
 */
@EActivity
public class SplashActivity extends Activity{
    private Context mContext;
    Connection connection;

    @RestService
    ChatDemoService chatDemoService;

    private Emitter.Listener mLoginSuccessListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            App.getInstance2().getGroupManager().getList(null, new EngineCallback<List<ChatGroup>>() {
                @Override
                public void onSuccess(List<ChatGroup> chatGroups) {
                    Log.d("main", "登陆聊天服务器成功！");
                    List<String> ids  = new ArrayList<String>();
                    App.getInstance2().setToken(Utils.getValue(mContext, Constants.AccessToken));

                    for (ChatGroup group : chatGroups){
                        ids.add(group.getGroupId());
                    }

                    if(!ids.isEmpty()){
                        refreshDemoGroups(ids);
                    }

                    Intent intent = new Intent(SplashActivity.this, MainActivity_.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.push_up_in,
                            R.anim.push_up_out);
                    finish();
                }

                @Override
                public void onError(ResponseError error) {
                    Log.i("login failed", error.getMessage());
                }
            });
        }
    };

    public void refreshDemoGroups(List<String> ids) {
        List<DemoGroup> groupList = null;
        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.put("groupIds[]", ids);

        try {
            groupList = chatDemoService.getGroupDemoInfo(map);
        } catch (Exception e) {

        }

        if (groupList != null) {
            GloableParams.ListGroupInfos = groupList;
            for (DemoGroup group : groupList) {
                GloableParams.GroupInfos.put(group.getGroupId(), group);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_start);
        initData();
        bindConnectionEvent();

        if(connection.isConnected()){
            Log.d("connected", "0");
            gotoLastView();
        }else{
            connection.bindOnce(Connection.State.Connected.getName(), new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d("connected", "1");
                    gotoLastView();
                }
            });
        }
    }

    private void gotoLastView(){
        int RunCount = Utils.getIntValue(this, "RUN_COUNT");
        if (RunCount == 0) {
            // TODO 引导页面
        } else {
            Utils.putIntValue(this, "RUN_COUNT", RunCount++);
        }
        Boolean isLogin = Utils.getBooleanValue(SplashActivity.this,
                Constants.LoginState);
        if (isLogin) {
            // Intent intent = new Intent(this, UpdateService.class);
            // startService(intent);
            getLogin();
        } else {
            mHandler.sendEmptyMessage(0);
        }
    }

    private void initData() {
        GloableParams.ListGroupInfos = new ArrayList<DemoGroup>();
        GloableParams.GroupInfos = new HashMap<String, DemoGroup>();
    }

    private void getLogin() {
        String name = Utils.getValue(this, Constants.User_ID);
        String pwd = Utils.getValue(this, Constants.PWD);
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(name))
            getChatserive(name, pwd);
        else {
            Utils.RemoveValue(SplashActivity.this, Constants.LoginState);
            mHandler.sendEmptyMessageDelayed(0, 600);
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Boolean isLogin = Utils.getBooleanValue(SplashActivity.this,
                    Constants.LoginState);
            Intent intent = new Intent();
            if (isLogin) {
                intent.setClass(SplashActivity.this, MainActivity_.class);
            } else {
                intent.setClass(SplashActivity.this, LoginActivity_.class);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
            finish();
        }
    };

    private void getChatserive(final String userName, final String password) {
        Log.d("loging", "------------------");
        App.getInstance2().getChatManager().bindOnce(ChatManager.EVENT_LOGIN_SUCCEEDED, mLoginSuccessListener);
        App.getInstance2().getChatManager().login(userName);
    }


    private void bindConnectionEvent() {
        connection = App.getInstance2().getEngine().getConnection();

        Emitter.Listener stateListener = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Connection.State state = (Connection.State)args[0];
                        String msg = "Connecting state: " + state.toString();
                        Utils.showLongToast(mContext, msg);
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
                        Utils.showLongToast(mContext, msg);
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
                        Utils.showLongToast(mContext, msg);
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
                        Utils.showLongToast(mContext, msg);
                    }
                });
            }
        });
    }
}