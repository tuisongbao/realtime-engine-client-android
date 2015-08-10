package com.tuisongbao.engine.demo.chat;

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

import com.github.nkzawa.emitter.Emitter;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.chat.user.entity.ChatUserPresenceData;
import com.tuisongbao.engine.demo.DemoApplication;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.chat.cache.LoginCache;
import com.tuisongbao.engine.demo.chat.fragment.ChatContactsFragment;
import com.tuisongbao.engine.demo.chat.fragment.ChatConversationsFragment;
import com.tuisongbao.engine.demo.chat.fragment.ChatSettingsFragment;
import com.tuisongbao.engine.utils.StrUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DashboardActivity extends FragmentActivity {
    private static final String TAG = "TSB" + "TSB" + DashboardActivity.class.getSimpleName();

    private TextView mConversationTextView, mContactsTextView, mSettingsTextView;
    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private ChatConversationsFragment mConversationsFragment;
    private ChatContactsFragment mContactsFragment;
    private ChatSettingsFragment mSettingsFragment;
    private int mCurrentPage = 0;
    private ChatManager mChatManager;

    private Map<String, Emitter.Listener> mListenersMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mChatManager = DemoApplication.engine.getChatManager();

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

        registerBroadcast();

        initAllListeners();
        manageListeners(true);
    }

    private void initAllListeners() {
        mListenersMap.put(ChatManager.EVENT_LOGIN_SUCCEEDED, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                showToaster("Auto login success");
            }
        });
        mListenersMap.put(ChatManager.EVENT_LOGIN_FAILED, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                showToaster("Auto login failed");
            }
        });
        mListenersMap.put(ChatManager.EVENT_PRESENCE_CHANGED, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                ChatUserPresenceData data = (ChatUserPresenceData) args[0];
                showToaster(data.getUserId() + " changed to " + data.getChangedTo());
            }
        });
        mListenersMap.put(ChatManager.EVENT_MESSAGE_NEW, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                if (mCurrentPage != 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mConversationTextView.setTextColor(getResources().getColor(R.color.red));
                        }
                    });
                }
            }
        });
    }

    private void manageListeners(boolean isBind) {
        Iterator<String> events = mListenersMap.keySet().iterator();
        while (events.hasNext()) {
            String event = events.next();
            Emitter.Listener listener = mListenersMap.get(event);
            if (isBind) {
                mChatManager.bind(event, listener);
            } else {
                mChatManager.unbind(event, listener);
            }
        }
    }

    private void showToaster(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
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
        manageListeners(false);
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
                        if (!StrUtils.isEmpty(userId)) {
                            ChatUser user = new ChatUser();
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
            if (ChatConversationActivity.BROADCAST_ACTION_MESSAGE_SENT.equals(action)) {
                ChatMessage message = intent.getParcelableExtra(ChatConversationActivity.BROADCAST_EXTRA_KEY_MESSAGE);
                mConversationsFragment.onMessageSent(message);
            }
        }
    };
}
