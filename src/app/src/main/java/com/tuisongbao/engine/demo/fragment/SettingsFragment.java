package com.tuisongbao.engine.demo.fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.GlobalParams;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.activity.LoginActivity_;
import com.tuisongbao.engine.demo.app.App;
import com.tuisongbao.engine.demo.utils.SpUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by user on 15-8-14.
 */
@EFragment(R.layout.fragment_settings)
public class SettingsFragment extends Fragment {

    @ViewById
    ImageView logo;

    @ViewById
    TextView userName;

    private SpUtil sp;

    @Click(R.id.chat_setting_logout)
    void logout(){
        App.getContext().getChatManager().logout(new EngineCallback<String>() {
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

        GlobalParams.ISLOGIN = false;
        sp.saveString(Constants.AUTOLOGINUSERNAME, null);
        Intent intent = new Intent(getActivity(), LoginActivity_.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Click(R.id.chat_setting_logout_clean)
    void logoutAndClean(){
        App.getContext().getChatManager().clearCache();
        logout();

    }

    @AfterViews
    void calledAfterViewInjection() {
        String target = App.getContext().getChatManager().getChatUser().getUserId();
        ImageLoader.getInstance().displayImage(Constants.USERAVATARURL + target, logo);
        userName.setText(target);
        sp = new SpUtil(getActivity());
    }

}
