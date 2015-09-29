package com.tuisongbao.engine.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.connection.Connection;
import com.tuisongbao.engine.demo.account.view.fragment.SettingsFragment_;
import com.tuisongbao.engine.demo.common.utils.Utils;
import com.tuisongbao.engine.demo.conversation.view.activity.ChatConversationActivity;
import com.tuisongbao.engine.demo.conversation.view.activity.ChatConversationActivity_;
import com.tuisongbao.engine.demo.conversation.view.fragment.ConversationsFragment_;
import com.tuisongbao.engine.demo.user.view.activity.SearchUserActivity;
import com.tuisongbao.engine.demo.user.view.activity.SearchUserActivity_;
import com.tuisongbao.engine.demo.user.view.fragment.ContactsFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@EActivity(R.layout.activity_main)
public class MainActivity extends FragmentActivity {

    @ViewsById({R.id.ib_conversations, R.id.ib_contact_list, R.id.ib_setting})
    List<ImageView> imageButtons;

    @ViewsById({R.id.tv_conversations, R.id.tv_contact_list, R.id.tv_setting})
    List<TextView> textViews;

    @ViewById(R.id.imgRight)
    ImageView img_right;

    @ViewById(R.id.txt_title)
    TextView txt_title;

    @ViewById(R.id.main_view_pager)
    ViewPager mViewPager;

    private String errTip;
    private Fragment[] fragments;
    private int index = 0;
    private int currentTabIndex = 0;// 当前fragment的index

    private int keyBackClickCount = 0;
    private ConversationsFragment_ conversationsFragment;
    private ContactsFragment_ contactsFragment;
    private SettingsFragment_ settingsFragment;
    private Activity activity;

    private static final int TARGET = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        errTip = "";
        App.getInstance().addActivity(this);
        bindConnection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        App.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.activityPaused();
    }

    @AfterViews
    void afterViews() {
        initTabView();
    }

    private void initTabView() {
        conversationsFragment = new ConversationsFragment_();
        contactsFragment = new ContactsFragment_();
        settingsFragment = new SettingsFragment_();

        fragments = new Fragment[]{conversationsFragment, contactsFragment,
                settingsFragment};

        imageButtons.get(0).setSelected(true);
        textViews.get(0).setTextColor(0xFF45C01A);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return fragments.length;
            }

            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                index = position;
                refreshTab();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        refreshTab();
    }

    public void onTabClicked(View view) {
        switch (view.getId()) {
            case R.id.re_conversations:
                index = 0;
                break;
            case R.id.re_contact_list:
                if (contactsFragment != null) {
                    contactsFragment.updateAvatar();
                }
                index = 1;
                break;
            case R.id.re_settings:
                index = 2;
                break;
        }
        refreshTab();
    }

    void refreshTab() {
        img_right.setVisibility(View.GONE);
        img_right.setOnClickListener(null);

        if (currentTabIndex != index) {
            mViewPager.setCurrentItem(index);
        }

        switch (index) {
            case 0:
                img_right.setVisibility(View.VISIBLE);
                txt_title.setText(getString(R.string.app_name) + errTip);
                img_right.setImageResource(R.drawable.icon_titleaddfriend);
                img_right.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity,
                                SearchUserActivity_.class);
                        startActivityForResult(intent, TARGET);
                    }
                });
                break;
            case 1:
                txt_title.setText(getString(R.string.contacts) + errTip);
                break;
            case 2:
                txt_title.setText(getString(R.string.setting) + errTip);
                break;
        }


        imageButtons.get(currentTabIndex).setSelected(false);
        // 把当前tab设为选中状态
        imageButtons.get(index).setSelected(true);
        textViews.get(currentTabIndex).setTextColor(0xFF999999);
        textViews.get(index).setTextColor(0xFF45C01A);
        currentTabIndex = index;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            switch (keyBackClickCount++) {
                case 0:
                    Toast.makeText(this, "再次按返回键退出", Toast.LENGTH_SHORT).show();
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            keyBackClickCount = 0;
                        }
                    }, 3000);
                    break;
                case 1:
                    App.getInstance().exit();
                    finish();
                    overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                    break;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    void bindConnection() {
        Connection connection = App.getInstance().getEngine().getConnection();
        connection.bind(Connection.State.Disconnected, disconnectedListener);
        connection.bind(Connection.State.Failed, disconnectedListener);
        connection.bind(Connection.EVENT_ERROR, disconnectedListener);
        connection.bind(Connection.State.Connected, connectedListener);
    }


    private Emitter.Listener connectedListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    errTip = "";
                    refreshTab();
                    conversationsFragment.errorItem.setVisibility(View.GONE);
                    if (conversationsFragment != null) {
                        conversationsFragment.refresh();
                    }
                }
            });
        }
    };


    private Emitter.Listener disconnectedListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    errTip = "(未连接)";
                    refreshTab();
                    final String st2 = getResources().getString(
                            R.string.the_current_network);
                    String msg = "Connection error," + args[0];
                    Utils.showShortToast(getApplicationContext(), msg);
                    conversationsFragment.errorItem.setVisibility(View.VISIBLE);
                    conversationsFragment.errorText.setText(st2);
                }
            });
        }
    };

    @OnActivityResult(TARGET)
    void onResult(int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK){
            return;
        }

        ArrayList<String> targets = data.getStringArrayListExtra(SearchUserActivity.USERNAMES);

        Intent intent = new Intent(this,
                ChatConversationActivity_.class);
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET, targets.get(0));
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE, ChatType.SingleChat);

        startActivity(intent);
    }
}