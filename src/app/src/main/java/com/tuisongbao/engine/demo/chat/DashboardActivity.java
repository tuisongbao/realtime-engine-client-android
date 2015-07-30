package com.tuisongbao.engine.demo.chat;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.TSBChatManager;
import com.tuisongbao.engine.chat.entity.TSBContactsUser;
import com.tuisongbao.engine.chat.entity.TSBMessage;
import com.tuisongbao.engine.common.TSBEngineBindCallback;
import com.tuisongbao.engine.common.TSBEngineCallback;
import com.tuisongbao.engine.connection.entity.TSBConnectionEvent;
import com.tuisongbao.engine.demo.DemoApplication;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.chat.cache.LoginCache;
import com.tuisongbao.engine.demo.chat.fragment.ChatContactsFragment;
import com.tuisongbao.engine.demo.chat.fragment.ChatConversationsFragment;
import com.tuisongbao.engine.demo.chat.fragment.ChatSettingsFragment;
import com.tuisongbao.engine.demo.chat.service.TSBMessageRevieveService;
import com.tuisongbao.engine.entity.TSBEngineConstants;
import com.tuisongbao.engine.util.StrUtil;

public class DashboardActivity extends FragmentActivity {
    private static final String TAG = "com.tuisongbao.engine.demo.DashboardActivity";
    private TextView mConversationTextView, mContactsTextView, mSettingsTextView;
    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private ChatConversationsFragment mConversationsFragment;
    private ChatContactsFragment mContactsFragment;
    private ChatSettingsFragment mSettingsFragment;
    private int mCurrentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mConversationTextView = (TextView) findViewById(R.id.dashboard_textview_conversations);
        mContactsTextView = (TextView) findViewById(R.id.dashboard_textview_contacts);
        mSettingsTextView = (TextView) findViewById(R.id.dashboard_textview_settings);
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
                    return mConversationsFragment;
                } else if (arg0 == 1) {
                    return mContactsFragment;
                }
                return mSettingsFragment;
            }
        };

        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                mCurrentPage = arg0;
                if (arg0 == 0) {
                    updateBackground(R.id.dashboard_textview_conversations);
                } else if (arg0 == 1) {
                    updateBackground(R.id.dashboard_textview_contacts);
                } else {
                    updateBackground(R.id.dashboard_textview_settings);
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        showFragment(R.id.dashboard_textview_contacts);

        mConversationTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(R.id.dashboard_textview_conversations);
            }
        });
        mContactsTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(R.id.dashboard_textview_contacts);
            }
        });
        mSettingsTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(R.id.dashboard_textview_settings);
            }
        });

        DemoApplication.engine.chatManager.bind(
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
                                                DashboardActivity.this,
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
        registerBroadcast();
    }

    private void initFragment() {
        mConversationsFragment = ChatConversationsFragment.getInstance();
        mContactsFragment = ChatContactsFragment.getInstance();
        mSettingsFragment = ChatSettingsFragment.getInstance();
    }

    private void showFragment(int textViewId) {

        switch (textViewId) {
        case R.id.dashboard_textview_conversations:
            mViewPager.setCurrentItem(0);
            break;

        case R.id.dashboard_textview_contacts:
            mViewPager.setCurrentItem(1);
            break;

        case R.id.dashboard_textview_settings:
            mViewPager.setCurrentItem(2);
            break;
        }
        updateBackground(textViewId);
    }

    private void updateBackground(int textViewId) {
        mConversationTextView.setBackgroundColor(getResources().getColor(R.color.gray));
        mContactsTextView.setBackgroundColor(getResources().getColor(R.color.gray));
        mSettingsTextView.setBackgroundColor(getResources().getColor(
                R.color.gray));

        switch (textViewId) {
        case R.id.dashboard_textview_conversations:
            mConversationTextView.setBackgroundColor(getResources().getColor(
                    R.color.blue));
            mConversationTextView.setTextColor(getResources().getColor(R.color.black));
            break;

        case R.id.dashboard_textview_contacts:
            mContactsTextView.setBackgroundColor(getResources().getColor(
                    R.color.blue));
            break;

        case R.id.dashboard_textview_settings:
            mSettingsTextView.setBackgroundColor(getResources().getColor(
                    R.color.blue));
            break;
        }
    }

    private void markNewMessage() {
        if (mCurrentPage != 0) {
            mConversationTextView.setTextColor(getResources().getColor(R.color.red));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterBroadcast();
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
                            TSBContactsUser user = new TSBContactsUser();
                            user.setUserId(userId);
                            LoginCache.addUser(user);
                            mContactsFragment.refresh();
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(TSBMessageRevieveService.BROADCAST_ACTION_RECEIVED_MESSAGE);
        filter.addAction(ChatConversationActivity.BROADCAST_ACTION_MESSAGE_SENT);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private void unregisterBroadcast() {
        unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!LoginCache.isLogin()) {
                return;
            }
            String action = intent.getAction();
            if (TSBMessageRevieveService.BROADCAST_ACTION_RECEIVED_MESSAGE.equals(action)) {
                markNewMessage();
                TSBMessage message = intent.getParcelableExtra(TSBMessageRevieveService.BROADCAST_EXTRA_KEY_MESSAGE);
                mConversationsFragment.onMessageReceived(message);
            } else if (ChatConversationActivity.BROADCAST_ACTION_MESSAGE_SENT.equals(action)) {
                TSBMessage message = intent.getParcelableExtra(ChatConversationActivity.BROADCAST_EXTRA_KEY_MESSAGE);
                mConversationsFragment.onMessageSent(message);
            }
        }
    };
}
