package com.tuisongbao.engine.demo.activity;

import android.app.ActionBar;

import com.tuisongbao.engine.demo.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

/**
 * Created by user on 15-8-28.
 */
@EActivity(R.layout.activity_create_group)
public class CreateGroupActivity extends BaseActivity{
    @AfterViews
    public void afterViews() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        setOverflowShowingAlways();
    }
}
