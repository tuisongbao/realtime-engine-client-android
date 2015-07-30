package com.tuisongbao.engine.demo.chat.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.tuisongbao.engine.chat.TSBChatManager;
import com.tuisongbao.engine.demo.DemoApplication;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.chat.LoginActivity;
import com.tuisongbao.engine.demo.chat.cache.LoginCache;

public class ChatSettingsFragment extends Fragment {

    private static ChatSettingsFragment mChatSettingFragment;
    private Button mLogoutButton;
    private View mRootView;

    public static ChatSettingsFragment getInstance() {
        if (null == mChatSettingFragment) {
            mChatSettingFragment = new ChatSettingsFragment();
        }
        return mChatSettingFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_settings, container,
                false);
        mLogoutButton = (Button)mRootView.findViewById(R.id.chat_setting_logout);
        mLogoutButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                DemoApplication.engine.chatManager.logout();
                Toast.makeText(getActivity(), "登出成功", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                LoginCache.clear();
                getActivity().finish();
            }
        });

        return mRootView;
    }
}
