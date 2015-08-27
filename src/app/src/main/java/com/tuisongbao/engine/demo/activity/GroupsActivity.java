package com.tuisongbao.engine.demo.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import android.widget.ListView;

import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.chat.group.ChatGroup;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.adapter.DemoGroupAdapter;
import com.tuisongbao.engine.demo.app.App;
import com.tuisongbao.engine.demo.entity.DemoGroup;
import com.tuisongbao.engine.demo.utils.AppToast;
import com.tuisongbao.engine.demo.utils.DemoGroupUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 15-8-26.
 */
@EActivity(R.layout.activity_group_list)
public class GroupsActivity extends BaseActivity{
    
    @ViewById(R.id.activity_groups_listView)
    ListView groupListView;

    @Bean
    DemoGroupUtil demoGroupUtil;
    
    List<DemoGroup> demoGroups;

    DemoGroupAdapter demoGroupAdapter;
    
    
    @AfterViews
    public void afterViews() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        setOverflowShowingAlways();
        actionBar.setTitle("群组");

        demoGroups = new ArrayList<>();
        demoGroupAdapter = new DemoGroupAdapter(demoGroups, this);
        
        groupListView.setAdapter(demoGroupAdapter);
        if(demoGroupUtil.getDemoGroups().isEmpty() || App.getContext().getChatGroups() == null){
            request();
        }else {
            loadGroupWithCache();
        }
    }


    public void request(){
        App.getContext().getGroupManager().getList(null, new EngineCallback<List<ChatGroup>>() {
            @Override
            public void onSuccess(List<ChatGroup> chatGroups) {
                if(chatGroups == null || chatGroups.isEmpty()){
                    return;
                }
                demoGroups = new ArrayList<>();
                for (ChatGroup group :
                        chatGroups) {
                    DemoGroup demoGroup = demoGroupUtil.getDemoGroup(group.getGroupId());

                    if (demoGroup == null) {
                        demoGroup = new DemoGroup(group.getGroupId(), "未命名群组", "");
                    }

                    demoGroups.add(demoGroup);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        demoGroupAdapter.refresh(demoGroups);
                    }
                });
            }

            @Override
            public void onError(ResponseError error) {

            }
        });
    }

    public void loadGroupWithCache(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (ChatGroup group :
                        App.getContext().getChatGroups()) {
                    DemoGroup demoGroup = demoGroupUtil.getDemoGroup(group.getGroupId());

                    if (demoGroup == null) {
                        demoGroup = new DemoGroup(group.getGroupId(), "未命名群组", "");
                    }

                    demoGroups.add(demoGroup);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        demoGroupAdapter.refresh(demoGroups);
                    }
                });
            }
        });
    }

    @ItemClick(R.id.activity_groups_listView)
    public void myListItemClicked(int position) {
        DemoGroup demoGroup = demoGroups.get(position);

        Intent intent = new Intent(this,
                ChatConversationActivity_.class);
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET, demoGroup.getGroupId());
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE, ChatType.GroupChat);

        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home://返回上一菜单页
                AppToast.getToast().show("返回上一页");
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                } else {
                    upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    NavUtils.navigateUpTo(this, upIntent);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
