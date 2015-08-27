package com.tuisongbao.engine.demo.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.adapter.DemoUserAdapter;
import com.tuisongbao.engine.demo.entity.DemoUser;
import com.tuisongbao.engine.demo.service.rest.UserService;
import com.tuisongbao.engine.demo.utils.AppToast;
import com.tuisongbao.engine.demo.utils.L;
import com.tuisongbao.engine.demo.utils.LogUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 15-8-27.
 */
@EActivity(R.layout.activity_add_user)
public class AddUserActivity extends BaseActivity {
    private static final String TAG = LogUtil.makeLogTag(AddUserActivity.class);
    @ViewById(R.id.activity_addUser_listView)
    ListView userList;

    DemoUserAdapter userAdapter;

    List<DemoUser> demoUsers;

    @RestService
    UserService userService;

    @AfterViews
    public void afterViews() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        setOverflowShowingAlways();
        demoUsers = new ArrayList<>();
        userAdapter = new DemoUserAdapter(this, demoUsers);
        userList.setAdapter(userAdapter);
    }

    @TextChange(R.id.search_user)
    void onTextChangesOnSearchUserTextView(CharSequence text, TextView searchUserTextView, int before, int start, int count) {
        L.i(TAG,"search:" + text);
        if("".equals(text.toString().trim())){
            demoUsers = new ArrayList<>();
            userAdapter.refresh(demoUsers);
        }else {
            searchUser(text.toString());
        }
    }

    @Background
    void searchUser(String username) {
        List<DemoUser> demoUserList = userService.getDemoUser(username);
        L.i(TAG, "demoList" + demoUserList);
        if(demoUserList != null){
            demoUsers = demoUserList;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    userAdapter.refresh(demoUsers);
                }
            });
        }
    }

    @ItemClick(R.id.activity_addUser_listView)
    void addUser(int position) {
        DemoUser user = demoUsers.get(position);

        Intent intent = new Intent(this,
                ChatConversationActivity_.class);
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET, user.getUsername());
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE, ChatType.SingleChat);

        startActivity(intent);
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
