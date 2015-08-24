package com.tuisongbao.engine.demo.activity;

import android.content.Intent;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.service.rest.UserService;
import com.tuisongbao.engine.demo.utils.AppToast;
import com.tuisongbao.engine.demo.utils.L;
import com.tuisongbao.engine.demo.utils.LogUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.androidannotations.api.rest.RestErrorHandler;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 15-8-18.
 */
@EActivity(R.layout.activity_register)
public class RegisterActivity extends BaseActivity{
    private static final String TAG = LogUtil.makeLogTag(RegisterActivity.class);

    @ViewById(R.id.regist_userName)
    EditText mEt_userName;

    @ViewById(R.id.regist_pwd)
    EditText mEt_pwd;

    @ViewById(R.id.regist_rpwd)
    EditText mEt_rpwd;

    private String username = "";
    private String password = "";
    private String userRpwd = "";
    private Vibrator vibrator;

    @RestService
    UserService userService;

    @AfterViews
    public void afterViews(){
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    @Click(R.id.regist_button_regist)
    public void regist(){
        if (checkValidity()) {
            submitRegist();
        }
    }

    private void getUserInfo() {
        username = mEt_userName.getText().toString().trim();
        password = mEt_pwd.getText().toString().trim();
        userRpwd = mEt_rpwd.getText().toString().trim();
    }

    private boolean checkValidity() {
        getUserInfo();
        if (TextUtils.isEmpty(username)) {
            mEt_userName.requestFocus();
            AppToast.getToast().show("用户名不能为空");
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.abc_shrink_fade_out_from_bottom);
            mEt_userName.startAnimation(shake);
            vibrator.vibrate(300);
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            mEt_pwd.requestFocus();
            AppToast.getToast().show("密码不能为空");
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.abc_shrink_fade_out_from_bottom);
            mEt_pwd.startAnimation(shake);
            vibrator.vibrate(300);
            return false;
        }
        if (password.length() < 6) {
            mEt_pwd.requestFocus();
            AppToast.getToast().show("密码格式不正确");
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.abc_shrink_fade_out_from_bottom);
            mEt_pwd.startAnimation(shake);
            vibrator.vibrate(300);
            return false;
        }
        if (!password.equals(userRpwd)) {
            mEt_rpwd.requestFocus();
            AppToast.getToast().show("重复密码和密码不相同");
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.abc_shrink_fade_out_from_bottom);
            mEt_rpwd.startAnimation(shake);
            vibrator.vibrate(300);
            return false;
        }
        return true;
    }



    @AfterInject
    void afterInject() {
        userService.setRestErrorHandler(new RestErrorHandler(){

            @Override
            public void onRestClientExceptionThrown(NestedRuntimeException e) {
                L.i(TAG, "123 ----------------------" + e);
            }
        });
    }

    @Background
    void submitRegist() {
        try {
            Map map =new HashMap();
            map.put("username", username);
            map.put("password", password);

            userService.setHeader("Context", MediaType.APPLICATION_JSON_VALUE);

            String registResult = userService.regist(map);

            L.i(TAG, "----------------------" + registResult);
            if(registResult == null){
                AppToast.getToast().show("注册失败");
                vibrator.vibrate(300);
            }else {
                AppToast.getToast().show("注册成功");
                Intent intent;
                intent = new Intent(this, LoginActivity_.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
