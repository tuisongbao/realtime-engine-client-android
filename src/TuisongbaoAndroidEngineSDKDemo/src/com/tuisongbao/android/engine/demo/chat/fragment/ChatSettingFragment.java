package com.tuisongbao.android.engine.demo.chat.fragment;

import android.support.v4.app.Fragment;

public class ChatSettingFragment extends Fragment {

    private static ChatSettingFragment mChatSettingFragment;

    public static ChatSettingFragment getInstance() {
        if (null == mChatSettingFragment) {
            mChatSettingFragment = new ChatSettingFragment();
        }
        return mChatSettingFragment;
    }
}
