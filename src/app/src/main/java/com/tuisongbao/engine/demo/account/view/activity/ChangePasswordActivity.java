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
import com.tuisongbao.engine.demo.App;
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
 * Created by user on 15-9-7.
 */
@EActivity(R.layout.activity_change_password)
public class ChangePasswordActivity extends BaseActivity{
    @ViewById(R.id.img_back)
    ImageView img_back;

    @ViewById(R.id.btn_submit)
    Button btnSubmit;

    @ViewById(R.id.et_old_password)
    TextView tvOldPassword;

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
        btnSubmit.setEnabled(false);
        String oldPassword = tvOldPassword.getText().toString();
        String password = tvPassword.getText().toString();
        String rePassword = tvRePassword.getText().toString();
        if(TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(password) || TextUtils.isEmpty(rePassword)){
            Utils.showShortToast(this, "密码不能为空");
            btnSubmit.setEnabled(true);
            return;
        }

        if(!password.equals(rePassword)){
            Utils.showShortToast(this, "密码不一致");
            btnSubmit.setEnabled(true);
            return;
        }

        RequestParams params = new RequestParams();
        LogUtils.i("change password: %s", oldPassword + "->" + password);
        params.put("currentPassword", oldPassword);
        params.put("newPassword", password);
        params.put("token", App.getInstance().getToken());
        getLoadingDialog("正在修改密码...  ").show();
        netClient.post(Constants.CHANGPASSWORDURL, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getLoadingDialog("正在修改密码").dismiss();
                        Utils.showShortToast(activity, "修改成功");
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
                    }
                });
                getLoadingDialog("正在修改密码").dismiss();
                btnSubmit.setEnabled(true);
            }
        }, true);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @TextChange({R.id.et_password, R.id.et_re_password, R.id.et_old_password})
    void textChange(){
        String oldPassword = tvOldPassword.getText().toString();
        String password = tvPassword.getText().toString();
        String rePassword = tvRePassword.getText().toString();

        if(TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(password) || TextUtils.isEmpty(rePassword)){
            btnSubmit.setEnabled(true);
            return;
        }

        btnSubmit.setBackground(getResources().getDrawable(
                R.drawable.btn_bg_green));
        btnSubmit.setEnabled(true);
    }

    @Click(R.id.img_back)
    void back() {
        Utils.finish(this);
    }
}
