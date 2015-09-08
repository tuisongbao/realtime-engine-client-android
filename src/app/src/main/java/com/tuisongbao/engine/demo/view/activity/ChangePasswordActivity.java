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

import com.apkfuns.logutils.LogUtils;
import com.juns.health.net.loopj.android.http.JsonHttpResponseHandler;
import com.juns.health.net.loopj.android.http.RequestParams;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.Constants;
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
 * Created by user on 15-9-7.
 */
@EActivity(R.layout.activity_change_password)
public class ChangePasswordActivity extends BaseActivity{
    @ViewById(R.id.img_back)
    ImageView img_back;

    @ViewById(R.id.btn_submit)
    Button btnsubmit;

    @ViewById(R.id.et_old_password)
    TextView tvoldPassword;

    @ViewById(R.id.et_password)
    TextView tvPassword;

    @ViewById(R.id.et_re_password)
    TextView tvRePassword;

    Activity activity;

    @AfterViews
    void afterViews() {
        activity = this;
        img_back.setVisibility(View.VISIBLE);
    }

    @Click(R.id.btn_submit)
    void submit(){
        btnsubmit.setEnabled(false);
        String oldPassword = tvoldPassword.getText().toString();
        String password = tvPassword.getText().toString();
        String rePassword = tvRePassword.getText().toString();
        if(TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(password) || TextUtils.isEmpty(rePassword)){
            Utils.showShortToast(this, "密码不能为空");
            btnsubmit.setEnabled(true);
            return;
        }

        if(!password.equals(rePassword)){
            Utils.showShortToast(this, "密码不一致");
            btnsubmit.setEnabled(true);
            return;
        }
        try{
            RequestParams params = new RequestParams();
            LogUtils.i("tag login", oldPassword + "--------------" + password);
            params.put("currentPassword", oldPassword);
            params.put("newPassword", password);
            params.put("token", App.getInstance2().getToken());
            getLoadingDialog("正在修改密码...  ").show();
            netClient.post(Constants.changPasswordUrl, params, new JsonHttpResponseHandler() {
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
                    getLoadingDialog("正在修改密码").dismiss();
                    btnsubmit.setEnabled(true);
                }
            });
        }catch (Exception e){
            Utils.showShortToast(this, "网络错误, 请重试");
            btnsubmit.setEnabled(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @TextChange({R.id.et_password, R.id.et_re_password, R.id.et_old_password})
    void textChange(){
        String oldPassword = tvoldPassword.getText().toString();
        String password = tvPassword.getText().toString();
        String rePassword = tvRePassword.getText().toString();
        LogUtils.i("submiter", oldPassword + password + rePassword);
        if(TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(password) || TextUtils.isEmpty(rePassword)){
            btnsubmit.setEnabled(true);
            return;
        }
        btnsubmit.setBackground(getResources().getDrawable(
                R.drawable.btn_bg_green));
        btnsubmit.setEnabled(true);
    }

    @Click(R.id.img_back)
    void back() {
        Utils.finish(this);
    }
}
