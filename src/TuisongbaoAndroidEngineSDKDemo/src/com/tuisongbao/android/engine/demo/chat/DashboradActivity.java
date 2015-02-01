package com.tuisongbao.android.engine.demo.chat;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupUser;
import com.tuisongbao.android.engine.common.TSBEngineBindCallback;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.connection.entity.TSBConnection;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.cache.LoginChache;
import com.tuisongbao.android.engine.demo.chat.fragment.ChatListFragment;
import com.tuisongbao.android.engine.demo.chat.fragment.ChatSettingFragment;
import com.tuisongbao.android.engine.demo.chat.fragment.ChatTalkFragment;
import com.tuisongbao.android.engine.entity.TSBEngineConstants;
import com.tuisongbao.android.engine.util.StrUtil;

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
        TSBChatManager.getInstance().bind(
                TSBEngineConstants.TSBENGINE_BIND_NAME_CHAT_PRESENCE_CHANGED,
                new TSBEngineBindCallback() {

                    @Override
                    public void onEvent(String bindName, String name,
                            final String data) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if (!StrUtil.isEmpty(data)) {
                                    try {
                                        JSONObject json = new JSONObject(data);
                                        String userId = json
                                                .getString("userId");
                                        String status = json
                                                .getString("changedTo");
                                        Toast.makeText(
                                                DashboradActivity.this,
                                                userId + " change to " + status,
                                                Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                });
        TSBEngine.connection.bind(TSBEngineConstants.TSBENGINE_BIND_NAME_CONNECTION_CONNECTED, new TSBEngineCallback<TSBConnection>() {
            
            @Override
            public void onSuccess(TSBConnection t) {
                runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        Toast.makeText(
                                DashboradActivity.this,
                                "你已成功链接上网络",
                                Toast.LENGTH_LONG).show();
                    }
                });
                
            }
            
            @Override
            public void onError(final int code, final String message) {
                runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        Toast.makeText(
                                DashboradActivity.this,
                                "你已成功断开网络[code=" + code + ";message=" + message + "]",
                                Toast.LENGTH_LONG).show();
                    }
                });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.group_add) {
            showAddUserDialog();
            return true;
        }
        return false;
    }

    private void showAddUserDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(
                R.layout.dialog_input_layout, null);
        new AlertDialog.Builder(this).setTitle("添加用户").setView(textEntryView)
                .setPositiveButton("添加", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userId = ((EditText)textEntryView.findViewById(R.id.dialog_input_edittext)).getText().toString();
                        if (!StrUtil.isEmpty(userId)) {
                            TSBChatGroupUser user = new TSBChatGroupUser();
                            user.setUserId(userId);
                            LoginChache.addUser(user);
                            mFragmentChatList.refresh();
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

}
