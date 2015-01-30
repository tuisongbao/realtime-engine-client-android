package com.tuisongbao.android.engine.demo.chat.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tuisongbao.android.engine.demo.R;

public class ChatSettingFragment extends Fragment {

    private static ChatSettingFragment mChatSettingFragment;
    private View mRootView;

    public static ChatSettingFragment getInstance() {
        if (null == mChatSettingFragment) {
            mChatSettingFragment = new ChatSettingFragment();
        }
        return mChatSettingFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_chat_setting, container,
                false);

        return mRootView;
    }
}
