package com.tuisongbao.engine.demo.activity;

import android.content.Intent;
import android.os.Vibrator;
import android.support.annotation.UiThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import com.github.nkzawa.emitter.Emitter;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.connection.Connection;
import com.tuisongbao.engine.demo.GlobalParams;
import com.tuisongbao.engine.demo.MainActivity_;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.app.App;
import com.tuisongbao.engine.demo.entity.Response;
import com.tuisongbao.engine.demo.service.rest.UserService;
import com.tuisongbao.engine.demo.utils.AppToast;
import com.tuisongbao.engine.demo.utils.L;
import com.tuisongbao.engine.demo.utils.LogUtil;
import com.tuisongbao.engine.demo.utils.SpUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.androidannotations.api.rest.RestErrorHandler;
import org.springframework.core.NestedRuntimeException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 15-8-14.
 */
@EActivity(R.layout.activity_login)
public class LoginActivity extends BaseActivity{
    private static final String TAG = LogUtil.makeLogTag(LoginActivity.class);

    @ViewById(R.id.login_et_userName)
    EditText mEt_userName;

    @ViewById(R.id.login_et_password)
    EditText mEt_password;

    @ViewById(R.id.login_btn_login)
    Button mBtn_login;

    @ViewById(R.id.login_btn_regist)
    Button mBtn_regist;

    @RestService
    UserService userService;

    private SpUtil sp;
    private Vibrator vibrator;

    private String username = "";
    private String password = "";

    private Emitter.Listener mLoginSuccessListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            GlobalParams.ISLOGIN = true;
            Intent intent = new Intent(LoginActivity.this, MainActivity_.class);
            startActivity(intent);
            finish();
        }
    };

    @UiThread
    void showMsg(String msg){
        AppToast.getToast().show("登陆推送宝成功");
    }

    private Emitter.Listener mLoginFailedListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            showMsg("登录失败");
        }
    };

    @Click(R.id.login_btn_login)
    public void login(){
        if(checkValidity()){
            submitLogin();
        }
    }

    @Click(R.id.login_btn_regist)
    public void regist(){
        L.d(TAG, "LoginActivity--->RegisterActivity");
        Intent intent = new Intent(this, RegisterActivity_.class);
        startActivity(intent);
//        finish();
    }

    @AfterViews
    public void afterViews(){
        String username = getIntent().getStringExtra("username");

        if (username != null) {
            mEt_userName.setText(username);
        }

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        bindConnectionEvent();
    }

    private void getUserInfo() {
        username = mEt_userName.getText().toString().trim();
        password = mEt_password.getText().toString().trim();
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
            mEt_password.requestFocus();
            AppToast.getToast().show("密码不能为空");
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.abc_shrink_fade_out_from_bottom);
            mEt_password.startAnimation(shake);
            vibrator.vibrate(300);
            return false;
        }
//        if (password.length() < 6) {
//            mEt_password.requestFocus();
//            AppToast.getToast().show("密码格式不正确");
//            Animation shake = AnimationUtils.loadAnimation(this, R.anim.abc_shrink_fade_out_from_bottom);
//            mEt_password.startAnimation(shake);
//            vibrator.vibrate(300);
//            return false;
//        }
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
    void submitLogin() {
        try {
            Map map =new HashMap();
            map.put("username", username);
            map.put("password", password);
            L.i(TAG, "logining ----------------------" + map);
            Response userResult = userService.login(map);

            if (userResult == null){
                AppToast.getToast().show("用户名或密码错误");
                L.i(TAG, "用户名或密码错误 ----------------------");
                return;
            }
            L.i(TAG, "logined ----------------------" + userResult);
            AppToast.getToast().show("登陆Demo成功");
            App.getContext().getChatManager().login(username);
        } catch (Exception e){
            L.i(TAG, "Exception ----------------------" + e);
            AppToast.getToast().show("登陆失败");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.getContext().getChatManager().bind(ChatManager.EVENT_LOGIN_SUCCEEDED, mLoginSuccessListener);
        App.getContext().getChatManager().bind(ChatManager.EVENT_LOGIN_FAILED, mLoginFailedListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.getContext().getChatManager().unbind(ChatManager.EVENT_LOGIN_SUCCEEDED, mLoginSuccessListener);
        App.getContext().getChatManager().unbind(ChatManager.EVENT_LOGIN_FAILED, mLoginFailedListener);
    }

    private void bindConnectionEvent() {
        Connection connection = App.getContext().getEngine().getConnection();

        Emitter.Listener stateListener = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                Connection.State state = (Connection.State)args[0];
                String msg = "Connecting state: " + state.toString();
                AppToast.getToast().show(msg);
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
                String msg = "Connecting in " + args[0] + " seconds";
                Log.i(TAG, msg);
                AppToast.getToast().show(msg);
            }
        });
        connection.bind(Connection.EVENT_STATE_CHANGED, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                String msg = "Connection state changed from " + args[0] + " to " + args[1];
                Log.i(TAG, msg);
                AppToast.getToast().show(msg);
            }
        });
        connection.bind(Connection.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                String msg =  "Connection error," + args[0];
                Log.i(TAG, msg);
                AppToast.getToast().show(msg);
            }
        });
    }
}
