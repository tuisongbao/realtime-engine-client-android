package com.tuisongbao.engine.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.tuisongbao.engine.demo.activity.LoginActivity_;
import com.tuisongbao.engine.demo.adapter.TabAdapter;
import com.tuisongbao.engine.demo.fragment.ContactsFragment;
import com.tuisongbao.engine.demo.fragment.ConversationsFragment_;
import com.tuisongbao.engine.demo.fragment.SettingsFragment_;
import com.tuisongbao.engine.demo.utils.AppToast;
import com.tuisongbao.engine.demo.utils.L;
import com.tuisongbao.engine.demo.utils.LogUtil;
import com.tuisongbao.engine.demo.utils.SpUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.WindowFeature;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@WindowFeature({ Window.FEATURE_NO_TITLE })
@EActivity(R.layout.activity_main)
public class MainActivity extends FragmentActivity implements RadioGroup.OnCheckedChangeListener {
    private static final String TAG = LogUtil.makeLogTag(MainActivity.class);

    @ViewById(R.id.main_view_pager)
    ViewPager mViewPager;

    @ViewById(R.id.main_rg_tab)
    RadioGroup mRg_tab;

    SpUtil sp;

    FragmentPagerAdapter adapter;

    private List<Fragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GlobalParams.activity = this;
        AppToast.getToast();
        if (!GlobalParams.ISLOGIN) {
            L.d(TAG, "MainActivity--->isLogin=" + GlobalParams.ISLOGIN);
            Intent intent = new Intent(this, LoginActivity_.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
//            finish();
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!GlobalParams.ISLOGIN) {
            finish();
        }
    }

    @AfterViews
    public void afterViews() {
        setOverflowShowingAlways();

        fragments = new ArrayList<>();
        fragments.add(new ConversationsFragment_());
        fragments.add(new ContactsFragment());
        fragments.add(new SettingsFragment_());
        adapter = new TabAdapter(getSupportFragmentManager(), fragments);
        // 防止第一个fragment被销毁
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(adapter);
        mRg_tab.setOnCheckedChangeListener(this);
        initData();
    }

    private void initData() {
        sp = new SpUtil(this);
        int id = sp.getInt(Constants.PAGENUMBER, 0);

        showPreviousPage(id);
    }

    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int id = 0;
        switch (checkedId) {
            case R.id.main_rb_conversations:
                id = 0;
                break;
            case R.id.main_rb_contacts:
                id = 1;
                break;
            case R.id.main_rb_settings:
                id = 2;
                break;
            default:
                id = 0;
                break;
        }
        sp.saveInt(Constants.PAGENUMBER, id);
        mViewPager.setCurrentItem(id);
    }

    /**
     * 通过反射得到Android的有无物理Menu键
     */
    private void setOverflowShowingAlways() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示上次退出时的页面
     *
     * @param id
     */
    private void showPreviousPage(int id) {
        RadioButton mRb_show = null;
        switch (id) {
            case 0:
                mRb_show = (RadioButton) findViewById(R.id.main_rb_conversations);
                break;
            case 1:
                mRb_show = (RadioButton) findViewById(R.id.main_rb_contacts);
                break;
            case 2:
                mRb_show = (RadioButton) findViewById(R.id.main_rb_settings);
                break;
            default:
                mRb_show = (RadioButton) findViewById(R.id.main_rb_conversations);
                break;
        }
        mRb_show.setChecked(true);
        mViewPager.setCurrentItem(id);
    }

}
