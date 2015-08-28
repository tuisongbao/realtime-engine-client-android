package com.tuisongbao.engine.demo.activity;

import android.content.Intent;

import com.tuisongbao.engine.demo.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

/**
 * Created by user on 15-8-28.
 */
@EActivity(R.layout.activity_user_info)
public class UserInfoActivity extends BaseActivity{
    @Click(R.id.user_info_change_password)
    public void gotoChangePassword(){
        Intent intant = new Intent(this, ChangePasswordActivity_.class);
        startActivity(intant);
    }
}
