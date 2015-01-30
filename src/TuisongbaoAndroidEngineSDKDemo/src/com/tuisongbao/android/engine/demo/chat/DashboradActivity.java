package com.tuisongbao.android.engine.demo.chat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.fragment.ChatListFragment;
import com.tuisongbao.android.engine.demo.chat.fragment.ChatSettingFragment;
import com.tuisongbao.android.engine.demo.chat.fragment.ChatTalkFragment;

public class DashboradActivity extends FragmentActivity {

    private TextView mTextViewTalk;
    private TextView mTextViewList;
    private TextView mTextViewSetting;
    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private ChatTalkFragment mFragmentChatTalk;
    private ChatListFragment mFragmentChatList;
    private ChatSettingFragment mFragmentChatSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mTextViewTalk = (TextView) findViewById(R.id.dashboard_textview_talk);
        mTextViewList = (TextView) findViewById(R.id.dashboard_textview_list);
        mTextViewSetting = (TextView) findViewById(R.id.dashboard_textview_setting);
        mViewPager = (ViewPager) findViewById(R.id.dashboard_view_pager);

        initFragment();
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public Fragment getItem(int arg0) {
                if (arg0 == 0) {
                    return mFragmentChatTalk;
                } else if (arg0 == 1) {
                    return mFragmentChatList;
                }
                return mFragmentChatSetting;
            }
        };

        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                if (arg0 == 0) {
                    updateBottomBackground(R.id.dashboard_textview_talk);
                } else if (arg0 == 1) {
                    updateBottomBackground(R.id.dashboard_textview_list);
                } else {
                    updateBottomBackground(R.id.dashboard_textview_setting);
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        showFragment(R.id.dashboard_textview_list);

        mTextViewTalk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(R.id.dashboard_textview_talk);
            }
        });
        mTextViewList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(R.id.dashboard_textview_list);
            }
        });
        mTextViewSetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(R.id.dashboard_textview_setting);
            }
        });
    }

    private void initFragment() {
        mFragmentChatTalk = ChatTalkFragment.getInstance();
        mFragmentChatList = ChatListFragment.getInstance();
        mFragmentChatSetting = ChatSettingFragment.getInstance();
    }

    private void showFragment(int textViewId) {

        switch (textViewId) {
        case R.id.dashboard_textview_talk:
            mViewPager.setCurrentItem(0);
            break;

        case R.id.dashboard_textview_list:
            mViewPager.setCurrentItem(1);
            break;

        case R.id.dashboard_textview_setting:
            mViewPager.setCurrentItem(2);
            break;
        }
        updateBottomBackground(textViewId);
    }

    private void updateBottomBackground(int textViewId) {
        mTextViewTalk.setBackgroundColor(getResources().getColor(R.color.gray));
        mTextViewList.setBackgroundColor(getResources().getColor(R.color.gray));
        mTextViewSetting.setBackgroundColor(getResources().getColor(
                R.color.gray));

        switch (textViewId) {
        case R.id.dashboard_textview_talk:
            mTextViewTalk.setBackgroundColor(getResources().getColor(
                    R.color.blue));
            break;

        case R.id.dashboard_textview_list:
            mTextViewList.setBackgroundColor(getResources().getColor(
                    R.color.blue));
            break;

        case R.id.dashboard_textview_setting:
            mTextViewSetting.setBackgroundColor(getResources().getColor(
                    R.color.blue));
            break;
        }
    }

}
