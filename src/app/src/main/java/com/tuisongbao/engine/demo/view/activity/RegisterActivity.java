package com.tuisongbao.engine.demo.view.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.juns.health.net.loopj.android.http.JsonHttpResponseHandler;
import com.juns.health.net.loopj.android.http.RequestParams;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.common.Utils;
import com.tuisongbao.engine.demo.service.ChatDemoService;
import com.tuisongbao.engine.demo.view.BaseActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.json.JSONObject;

/**
 * Created by user on 15-8-31.
 */
@EActivity(R.layout.activity_regist)
public class RegisterActivity extends BaseActivity{
    @ViewById(R.id.img_back)
    ImageView img_back;

    @ViewById(R.id.btn_regist)
    Button btnRegist;

    @ViewById(R.id.et_username)
    TextView tvUsername;

    @ViewById(R.id.et_password)
    TextView tvPassword;

    @ViewById(R.id.et_re_password)
    TextView tvRePassword;

    @RestService
    ChatDemoService chatDemoService;

    Activity activity;

    @AfterViews
    void afterViews() {
        activity = this;
        img_back.setVisibility(View.VISIBLE);
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
        try{
            RequestParams params = new RequestParams();
            Log.i("tag login", username + "--------------" + password);
            params.put("username", username);
            params.put("password", password);
            getLoadingDialog("正在注册...  ").show();
            netClient.post(Constants.RegistURL, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(JSONObject response) {
                    Utils.start_Activity(activity, LoginActivity_.class);
                }

                @Override
                public void onFailure(final Throwable e, final JSONObject errorResponse) {
                    if(errorResponse == null){
                        Utils.start_Activity(activity, LoginActivity_.class);
                        return;
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("error", e.getMessage() + errorResponse + "");
                            Utils.showShortToast(activity, "网络错误, 请重试");
                        }
                    });
                    getLoadingDialog("正在注册").dismiss();
                    btnRegist.setEnabled(true);
                }
            });
        }catch (Exception e){
            Utils.showShortToast(this, "网络错误, 请重试");
            btnRegist.setEnabled(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @TextChange({R.id.et_password, R.id.et_re_password, R.id.et_username})
    void textChange(){
        String username = tvUsername.getText().toString();
        String password = tvPassword.getText().toString();
        String rePassword = tvRePassword.getText().toString();
        Log.i("register", username + password + rePassword);
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
