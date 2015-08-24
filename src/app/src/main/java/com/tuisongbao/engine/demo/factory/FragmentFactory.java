package com.tuisongbao.engine.demo.factory;

import android.support.v4.app.Fragment;

import com.tuisongbao.engine.demo.fragment.ContactsFragment;
import com.tuisongbao.engine.demo.fragment.ConversationsFragment;
import com.tuisongbao.engine.demo.fragment.SettingsFragment;

/**
 * Created by user on 15-8-14.
 */
public class FragmentFactory {
    public static Fragment getInstanceByIndex(int index) {
        Fragment fragment = null;
        switch (index) {
            case 0:// 会话列表
                fragment = new ConversationsFragment();
                break;
            case 1:// 通讯录
                fragment = new ContactsFragment();
                break;
            case 2:// 设置
                fragment = new SettingsFragment();
                break;
        }
        return fragment;
    }
}
