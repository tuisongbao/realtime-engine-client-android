package com.tuisongbao.engine.demo.account.view.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.common.utils.Utils;
import com.tuisongbao.engine.demo.common.view.activity.BaseActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;

/**
 * Created by user on 15-8-31.
 */
@EActivity(R.layout.activity_regist)
public class RegisterActivity extends BaseActivity{
    @ViewById(R.id.img_back)
    ImageView imgBack;

    @ViewById(R.id.btn_regist)
    Button btnRegist;

    @ViewById(R.id.et_username)
    TextView tvUsername;

    @ViewById(R.id.et_password)
    TextView tvPassword;

    @ViewById(R.id.et_re_password)
    TextView tvRePassword;

    Activity activity;

    @AfterViews
    void afterViews() {
        activity = this;
        imgBack.setVisibility(View.VISIBLE);
    }

    @Click(R.id.btn_regist)
    void regist(){
        btnRegist.setEnabled(false);
        String username = tvUsername.getText().toString();
        String password = tvPassword.getText().toString();
        String rePassword = tvRePassword.getText().toString();
        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(rePassword)){
            Utils.showShortToast(this, "用户名密码不能为空");
            btnRegist.setEnabled(true);
            return;
        }

        if(!password.equals(rePassword)){
            Utils.showShortToast(this, "密码不一致");
            btnRegist.setEnabled(true);
            return;
        }

        RequestParams params = new RequestParams();
        LogUtils.i("login", username + ":" + password);
        params.put("username", username);
        params.put("password", password);
        getLoadingDialog("正在注册...  ").show();
        netClient.post(Constants.REGISTURL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getLoadingDialog("正在注册").dismiss();
                        Utils.showShortToast(activity, "注册成功");
                    }
                });
                Utils.start_Activity(activity, LoginActivity_.class);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, final byte[] responseBody, Throwable error) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showShortToast(activity, new String(responseBody));
                        getLoadingDialog("正在注册").dismiss();
                    }
                });
                btnRegist.setEnabled(true);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @TextChange({R.id.et_password, R.id.et_re_password, R.id.et_username})
    void textChange(){
        String username = tvUsername.getText().toString();
        String password = tvPassword.getText().toString();
        String rePassword = tvRePassword.getText().toString();
        LogUtils.i("register", username + password + rePassword);
        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(rePassword)){
            btnRegist.setEnabled(true);
            return;
        }
        btnRegist.setBackground(getResources().getDrawable(
                R.drawable.btn_bg_green));
        btnRegist.setEnabled(true);
    }

    @Click(R.id.img_back)
    void back() {
        Utils.finish(this);
    }
}
