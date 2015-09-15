package com.tuisongbao.engine.demo.view.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.apkfuns.logutils.LogUtils;
import com.github.nkzawa.emitter.Emitter;
import com.juns.health.net.loopj.android.http.JsonHttpResponseHandler;
import com.juns.health.net.loopj.android.http.RequestParams;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.group.ChatGroup;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.GloableParams;
import com.tuisongbao.engine.demo.MainActivity_;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.bean.DemoGroup;
import com.tuisongbao.engine.demo.common.Utils;
import com.tuisongbao.engine.demo.service.ChatDemoService;
import com.tuisongbao.engine.demo.view.BaseActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.LinkedMultiValueMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 15-8-31.
 */
@EActivity(R.layout.activity_login)
public class LoginActivity extends BaseActivity {
    @ViewById(R.id.et_username)
    EditText etUserName;

    @ViewById(R.id.et_password)
    EditText etPassword;

    @ViewById(R.id.img_back)
    ImageView backButton;

    @ViewById(R.id.btn_login)
    Button loginBtn;

    @RestService
    ChatDemoService chatDemoService;

    Context mContext;

    private Emitter.Listener mLoginSuccessListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Utils.putBooleanValue(LoginActivity.this,
                    Constants.LoginState, true);
            Utils.putValue(LoginActivity.this,
                    Constants.User_ID, etUserName.getText().toString());
            Utils.putValue(LoginActivity.this,
                    Constants.PWD, etPassword.getText().toString());
            Log.d("main", "登陆聊天服务器成功！");
            App.getInstance2().getGroupManager().getList(null, new EngineCallback<List<ChatGroup>>() {
                @Override
                public void onSuccess(List<ChatGroup> chatGroups) {
                    Log.d("main", "登陆聊天服务器成功！");
                    List<String> ids = new ArrayList<String>();
                    App.getInstance2().setToken(App.getInstance2().getToken());

                    for (ChatGroup group : chatGroups) {
                        ids.add(group.getGroupId());
                    }

                    if (!ids.isEmpty()) {
                        refreshDemoGroups(ids);
                    }
                    Utils.start_Activity(LoginActivity.this, MainActivity_.class);
                }

                @Override
                public void onError(ResponseError error) {
                    LogUtils.i("login failed", error.getMessage());
                }
            });
        }
    };

    public void refreshDemoGroups(List<String> ids) {
        List<DemoGroup> groupList = null;
        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.put("groupIds[]", ids);

        try {
            String token = App.getInstance2().getToken();
            groupList = chatDemoService.getGroupDemoInfo(map, token);
        } catch (Exception e) {

        }

        if (groupList != null) {
            GloableParams.ListGroupInfos = groupList;
            for (DemoGroup group : groupList) {
                GloableParams.GroupInfos.put(group.getGroupId(), group);
            }
        }
    }

    @AfterViews
    void afterViews() {
        mContext = this;
        backButton.setVisibility(View.VISIBLE);
    }

    // 退出应用
    @Click(R.id.img_back)
    void back() {
        Utils.finish(LoginActivity.this);
    }

    @Click(R.id.btn_registe)
    void registe() {
        startActivity(new Intent(this, RegisterActivity_.class));
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
    }

    @Click(R.id.btn_login)
    void login() {
        loginBtn.setEnabled(false);
        String userName = etUserName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getLoadingDialog("正在登录...").show();
            }
        });
        App.getInstance2().getChatManager().bindOnce(ChatManager.EVENT_LOGIN_SUCCEEDED, mLoginSuccessListener);
        getLogin(userName, password);
    }

    private void getLogin(final String userName, final String password) {
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password)) {
            RequestParams params = new RequestParams();
            LogUtils.i("tag login", userName + "--------------" + password);
            params.put("username", userName);
            params.put("password", password);

            getLoadingDialog("正在登录...  ").show();
            netClient.post(Constants.Login_URL, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(JSONObject response) {
                    LogUtils.i("login success", userName + "--------------" + response);
                    String token = null;
                    try {
                        token = response.getString("token");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (token != null) {
                        App.getInstance2().setToken(token);
                        Utils.putValue(LoginActivity.this,
                                Constants.AccessToken, token);
                    }
                    App.getInstance2().getChatManager().login(userName);
                }

                @Override
                public void onFailure(Throwable e, JSONObject errorResponse) {
                    getLoadingDialog("正在登录").dismiss();
                    loginBtn.setEnabled(true);
                }
            });
        } else {
            Utils.showLongToast(this, "请填写账号或密码！");
            loginBtn.setEnabled(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @TextChange({R.id.et_password, R.id.et_username})
    void checkText() {
        boolean Sign2 = etUserName.getText().length() > 4;
        boolean Sign3 = etPassword.getText().length() > 0;
        if (Sign2 & Sign3) {
            loginBtn.setBackground(getResources().getDrawable(
                    R.drawable.btn_bg_green));
            loginBtn.setEnabled(true);
        } else {
            loginBtn.setBackground(getResources().getDrawable(
                    R.drawable.btn_enable_green));
            loginBtn.setTextColor(0xFFD0EFC6);
            loginBtn.setEnabled(false);
        }
    }
}
