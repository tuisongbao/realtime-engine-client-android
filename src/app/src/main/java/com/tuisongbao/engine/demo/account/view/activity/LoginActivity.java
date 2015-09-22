package com.tuisongbao.engine.demo.account.view.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.apkfuns.logutils.LogUtils;
import com.github.nkzawa.emitter.Emitter;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.MainActivity_;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.common.utils.Utils;
import com.tuisongbao.engine.demo.common.view.activity.BaseActivity;
import com.tuisongbao.engine.demo.common.view.widght.dialog.FlippingLoadingDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

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

    Context mContext;

    private Emitter.Listener mLoginSuccessListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Utils.putBooleanValue(LoginActivity.this,
                    Constants.LOGINSTATE, true);
            Utils.putValue(LoginActivity.this,
                    Constants.USERNAME, etUserName.getText().toString());
            Utils.putValue(LoginActivity.this,
                    Constants.PWD, etPassword.getText().toString());
            Utils.start_Activity(LoginActivity.this, MainActivity_.class);
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Boolean isLogin = Utils.getBooleanValue(this,
                Constants.LOGINSTATE);

        LogUtils.d("isLogin", isLogin);

        if (isLogin) {
            String name = Utils.getValue(this, Constants.USERNAME);
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(name)){
                LogUtils.i("User login", name);
                String token = Utils.getValue(this, Constants.ACCESSTOKEN);
                App.getInstance().setToken(token);
                App.getInstance().getChatManager().bindOnce(ChatManager.EVENT_LOGIN_SUCCEEDED, mLoginSuccessListener);
                App.getInstance().getChatManager().login(name);
            }
            else {
                Utils.RemoveValue(this, Constants.LOGINSTATE);
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
        App.getInstance().getChatManager().bindOnce(ChatManager.EVENT_LOGIN_SUCCEEDED, mLoginSuccessListener);
        getLogin(userName, password);
    }

    private void getLogin(final String userName, final String password) {
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password)) {
            RequestParams params = new RequestParams();
            LogUtils.i("login", userName + ":" + password);
            params.put("username", userName);
            params.put("password", password);

            final FlippingLoadingDialog dialog = getLoadingDialog("正在登录...  ");

            if (!this.isFinishing() && !dialog.isShowing()) {
                dialog.show();
            }

            netClient.post(Constants.LOGINURL, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    LogUtils.i("login success", userName + ", " + response);
                    String token = null;
                    try {
                        token = response.getString("token");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (token != null) {
                        App.getInstance().setToken(token);
                        Utils.putValue(LoginActivity.this,
                                Constants.ACCESSTOKEN, token);
                    }
                    App.getInstance().getChatManager().login(userName);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                    dialog.dismiss();
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
        boolean Sign2 = etUserName.getText().length() > 0;
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
