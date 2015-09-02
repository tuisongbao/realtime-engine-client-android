package com.tuisongbao.engine.demo.view.fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.common.Utils;
import com.tuisongbao.engine.demo.net.NetClient;
import com.tuisongbao.engine.demo.view.activity.LoginActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by user on 15-9-1.
 */
@EFragment(R.layout.fragment_setting)
public class SettingsFragment extends Fragment{
    @ViewById(R.id.head)
    ImageView head;

    @ViewById(R.id.tvname)
    TextView tvname;

    @AfterViews
    void afterViews(){
        String username = App.getInstance2().getChatUser().getUserId();
        tvname.setText(username);
        NetClient.getIconBitmap(head, Constants.USERAVATARURL + username);
    }

    @Click(R.id.btn_logout)
    void logout(){
        App.getInstance2().getChatManager().logout(new EngineCallback<String>() {
            @Override
            public void onSuccess(String s) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "登出成功", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(ResponseError error) {

            }
        });

        Utils.RemoveValue(getActivity(), Constants.LoginState);
        Utils.RemoveValue(getActivity(), Constants.UserInfo);
        Intent intent = new Intent(getActivity(), LoginActivity_.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Click(R.id.btn_logout_clean)
    void logoutAndClean(){
        App.getInstance2().getChatManager().clearCache();
        logout();

    }
}
