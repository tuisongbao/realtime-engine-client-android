package com.tuisongbao.android.engine.demo.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.tuisongbao.android.engine.demo.R;

public class LoginActivity extends Activity {

    private EditText mEditTextAccount;
    private EditText mEditTextPassword;
    private Button mButtonLogin;
    private Button mButtonReigster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mButtonLogin = (Button) findViewById(R.id.login_textview_login);
        mButtonLogin.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,
                        DashboradActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
