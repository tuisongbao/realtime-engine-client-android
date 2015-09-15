package com.tuisongbao.engine.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.apkfuns.logutils.LogUtils;
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
import java.util.List;

/**
 * Created by user on 15-8-31.
 */
@EActivity
public class SplashActivity extends Activity{
    private Context mContext;
    private Connection connection;

    @RestService
    ChatDemoService chatDemoService;

    private Emitter.Listener mLoginSuccessListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            App.getInstance2().getGroupManager().getList(null, new EngineCallback<List<ChatGroup>>() {
                @Override
                public void onSuccess(List<ChatGroup> chatGroups) {
                    LogUtils.d("登陆推送宝服务器成功");
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
                    Utils.showShortToast(mContext, "登陆失败");
                    LogUtils.e("login failed %s", error.getMessage());
                }
            });
        }
    };

    // TODO 抽取一个 DemoGroup 缓存处理的工具类
    public void refreshDemoGroups(List<String> ids) {
        List<DemoGroup> groupList = null;
        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.put("groupIds[]", ids);

        try {
            String token = App.getInstance2().getToken();
            groupList = chatDemoService.getGroupDemoInfo(map, token);
        } catch (Exception e) {

        }

        LogUtils.d("refreshDemoGroups: %s", groupList);

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
        bindConnectionEvent();

        if(connection.isConnected()){
            gotoLastView();
        }else{
            connection.bindOnce(Connection.State.Connected.getName(), new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    gotoLastView();
                }
            });
        }
    }

    private void gotoLastView(){
        int RunCount = Utils.getIntValue(this, "RUN_COUNT");
        LogUtils.d("RunCount %d", RunCount);

        if (RunCount == 0) {
            // TODO 引导页面
        } else {
            Utils.putIntValue(this, "RUN_COUNT", RunCount++);
        }

        Boolean isLogin = Utils.getBooleanValue(SplashActivity.this,
                Constants.LoginState);

        LogUtils.d("isLogin", isLogin);

        if (isLogin) {
            String name = Utils.getValue(this, Constants.User_ID);
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(name)){
                LogUtils.i("User login", name);
                App.getInstance2().getChatManager().bindOnce(ChatManager.EVENT_LOGIN_SUCCEEDED, mLoginSuccessListener);
                App.getInstance2().getChatManager().login(name);
            }
            else {
                Utils.RemoveValue(SplashActivity.this, Constants.LoginState);
            }
        } else {
            Intent intent = new Intent();
            intent.setClass(SplashActivity.this, LoginActivity_.class);
            startActivity(intent);
            overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
            finish();
        }
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