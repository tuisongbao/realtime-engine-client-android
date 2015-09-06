package com.tuisongbao.engine.demo.view.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.github.nkzawa.emitter.Emitter;
import com.juns.health.net.loopj.android.http.JsonHttpResponseHandler;
import com.juns.health.net.loopj.android.http.RequestParams;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.MainActivity_;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.common.Utils;
import com.tuisongbao.engine.demo.view.BaseActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

/**
 * Created by user on 15-8-31.
 */
@EActivity(R.layout.activity_login)
public class LoginActivity extends BaseActivity{
    @ViewById(R.id.et_username)
    EditText etUserName;

    @ViewById(R.id.et_password)
    EditText etPassword;

    @ViewById(R.id.img_back)
    ImageView backButton;

    @ViewById(R.id.btn_login)
    Button loginBtn;

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
            Intent intent = new Intent(LoginActivity.this, MainActivity_.class);
            startActivity(intent);
            overridePendingTransition(R.anim.push_up_in,
                    R.anim.push_up_out);
            finish();
        }
    };

    @AfterViews
    void afterViews(){
        backButton.setVisibility(View.VISIBLE);
    }

    // 退出应用
    @Click(R.id.img_back)
    void back(){
        Utils.finish(LoginActivity.this);
    }

    @Click(R.id.btn_registe)
    void registe(){
        startActivity(new Intent(this, RegisterActivity_.class));
        overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
    }

    @Click(R.id.btn_login)
    void login(){
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
            Log.i("tag login", userName + "--------------" + password);
            params.put("username", userName);
            params.put("password", password);

            getLoadingDialog("正在登录...  ").show();
            netClient.post(Constants.Login_URL, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.i("login success", userName + "--------------" + response);
                    App.getInstance2().getChatManager().login(userName);
                }

                @Override
                public void onFailure(Throwable e, JSONObject errorResponse) {
                    getLoadingDialog("正在登录").dismiss();
                }
            });
        } else {
            Utils.showLongToast(this, "请填写账号或密码！");
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @TextChange({R.id.et_password, R.id.et_username})
    void checkText(){
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
