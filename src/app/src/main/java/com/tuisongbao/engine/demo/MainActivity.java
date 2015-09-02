package com.tuisongbao.engine.demo;

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
import com.tuisongbao.engine.connection.Connection;
import com.tuisongbao.engine.demo.common.Utils;
import com.tuisongbao.engine.demo.view.fragment.ContactsFragment_;
import com.tuisongbao.engine.demo.view.fragment.ConversationsFragment_;
import com.tuisongbao.engine.demo.view.fragment.SettingsFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by user on 15-8-31.
 */
@EActivity(R.layout.activity_main)
public class MainActivity extends FragmentActivity {

    @ViewsById({ R.id.ib_conversations, R.id.ib_contact_list, R.id.ib_setting })
    List<ImageView> imagebuttons;

    @ViewsById({ R.id.tv_conversations, R.id.tv_contact_list, R.id.tv_setting })
    List<TextView> textviews;

    @ViewById(R.id.img_right)
    ImageView img_right;

    @ViewById(R.id.txt_title)
    TextView txt_title;

    private Fragment[] fragments;
    private String connectMsg = "";
    private int index;
    private int currentTabIndex;// 当前fragment的index
    private int keyBackClickCount = 0;

    ConversationsFragment_ conversationsFragment;
    ContactsFragment_ contactsFragment;
    SettingsFragment_ settingsFragment;

    @ViewById(R.id.main_view_pager)
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getInstance2().addActivity(this);
        bindConnection();
    }

    @AfterViews
    void afterViews(){
        initTabView();
    }

    void bindConnection(){
        Connection connection = App.getInstance2().getEngine().getConnection();
        connection.bind(Connection.State.Disconnected, disconnectedListener);
        connection.bind(Connection.State.Failed, disconnectedListener);
        connection.bind(Connection.EVENT_ERROR, disconnectedListener);
        connection.bind(Connection.State.Connected, connectedListener);
    }

    private void initTabView() {
        conversationsFragment = new ConversationsFragment_();
        contactsFragment = new ContactsFragment_();
        settingsFragment = new SettingsFragment_();

        fragments = new Fragment[] { conversationsFragment, contactsFragment,
                settingsFragment };

        imagebuttons.get(0).setSelected(true);
        textviews.get(0).setTextColor(0xFF45C01A);
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
    }

    public void onTabClicked(View view) {
        switch (view.getId()) {
            case R.id.re_conversations:
                index = 0;
                if (conversationsFragment != null) {
                    conversationsFragment.refresh();
                }
                break;
            case R.id.re_contact_list:
                index = 1;
                break;
            case R.id.re_settings:
                index = 2;
                break;
        }
        refreshTab();
    }

    void refreshTab(){
        img_right.setVisibility(View.GONE);

        if (currentTabIndex != index) {
            mViewPager.setCurrentItem(index);
        }

        switch (index) {
            case 0:
                img_right.setVisibility(View.VISIBLE);
                txt_title.setText(R.string.app_name);
                img_right.setImageResource(R.drawable.icon_add);
                break;
            case 1:
                txt_title.setText(R.string.contacts);
                img_right.setVisibility(View.VISIBLE);
                img_right.setImageResource(R.drawable.icon_titleaddfriend);
                break;
            case 2:
                txt_title.setText(R.string.setting);
                break;
        }


        imagebuttons.get(currentTabIndex).setSelected(false);
        // 把当前tab设为选中状态
        imagebuttons.get(index).setSelected(true);
        textviews.get(currentTabIndex).setTextColor(0xFF999999);
        textviews.get(index).setTextColor(0xFF45C01A);
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
                    App.getInstance2().exit();
                    finish();
                    overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                    break;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private Emitter.Listener connectedListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                connectMsg = getString(R.string.app_name);
                txt_title.setText(connectMsg);
                conversationsFragment.errorItem.setVisibility(View.GONE);
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
                    connectMsg = "推送宝(未连接)";
                    txt_title.setText(connectMsg);
                    final String st1 = getResources().getString(
                            R.string.Less_than_chat_server_connection);
                    final String st2 = getResources().getString(
                            R.string.the_current_network);
                    String msg =  "Connection error," + args[0];
                    Utils.showLongToast(getApplicationContext(), msg);
                    conversationsFragment.errorItem.setVisibility(View.VISIBLE);
                    conversationsFragment.errorText.setText(st2);
                }
            });
        }
    };
}
