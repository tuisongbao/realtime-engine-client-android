package com.tuisongbao.engine.demo.view.activity;

/**
 * Created by user on 15-9-2.
 */

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.widget.ListView;
import android.widget.TextView;

import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.chat.group.ChatGroup;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.adpter.DemoUserAdapter;
import com.tuisongbao.engine.demo.bean.DemoUser;
import com.tuisongbao.engine.demo.chat.ChatConversationActivity;
import com.tuisongbao.engine.demo.common.Utils;
import com.tuisongbao.engine.demo.service.ChatDemoService;
import com.tuisongbao.engine.demo.view.BaseActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 15-8-27.
 */
@EActivity(R.layout.activity_add_user_to_group)
@OptionsMenu(R.menu.add_user_to_group)
public class AddUserToGroup extends BaseActivity {
    public static final String EXTRA_GROUP = "chatGroup";
    @ViewById(R.id.activity_addUser_search_listView)
    ListView userList;

    @ViewById(R.id.activity_addUser_listView)
    ListView adduserList;

    DemoUserAdapter userAdapter;

    DemoUserAdapter addUserAdapter;

    List<DemoUser> demoUsers;

    List<DemoUser> addDemoUsers;

    @ViewById(R.id.search_user)
    TextView search;
    @RestService
    ChatDemoService userService;

    List<String> oldDemoUserNames;

    ChatGroup chatGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        oldDemoUserNames = intent.getStringArrayListExtra("oldDemoUserNames");
        String chatGroupString = getIntent().getStringExtra(EXTRA_GROUP);
        chatGroup = ChatGroup.deserialize(App.getInstance2().getEngine(), chatGroupString);
    }

    @AfterViews
    public void afterViews() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        demoUsers = new ArrayList<>();
        userAdapter = new DemoUserAdapter(this, demoUsers);
        userList.setAdapter(userAdapter);
        addDemoUsers = new ArrayList<>();
        addUserAdapter = new DemoUserAdapter(this, addDemoUsers);
        adduserList.setAdapter(addUserAdapter);
    }

    @TextChange(R.id.search_user)
    void onTextChangesOnSearchUserTextView(CharSequence text, TextView searchUserTextView, int before, int start, int count) {
        if ("".equals(text.toString().trim())) {
            demoUsers = new ArrayList<>();
            userAdapter.refresh(demoUsers);
        } else {
            searchUser(text.toString());
        }
    }

    @Background
    void searchUser(String username) {
        List<DemoUser> demoUserList = userService.getDemoUser(username);
        if (demoUserList != null) {
            List<DemoUser> newUsers = null;
            if (oldDemoUserNames != null) {
                newUsers = new ArrayList<>();
                for (DemoUser demoUser : demoUserList) {
                    if (oldDemoUserNames.contains(demoUser.getUsername())) {
                        continue;
                    }
                    newUsers.add(demoUser);
                }
            } else {
                newUsers = demoUserList;
            }

            demoUsers = newUsers;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    userAdapter.refresh(demoUsers);
                }
            });
        }
    }

    @ItemClick(R.id.activity_addUser_search_listView)
    void addUser(int position) {
        DemoUser user = demoUsers.get(position);
        addDemoUsers.add(user);
        addUserAdapter.refresh(addDemoUsers);
        demoUsers.remove(user);
        userAdapter.refresh(demoUsers);
    }

    @ItemClick(R.id.activity_addUser_listView)
    void removeAddUser(int position) {
        DemoUser user = addDemoUsers.get(position);
        addDemoUsers.remove(user);
        addUserAdapter.refresh(addDemoUsers);
        if (!"".equals(search.getText().toString())) {
            searchUser(search.getText().toString());
        }
    }

    void back() {

        Intent upIntent = NavUtils.getParentActivityIntent(this);

        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            Utils.finish(AddUserToGroup.this);
        } else {
            upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            NavUtils.navigateUpTo(this, upIntent);
        }
    }

    @OptionsItem(R.id.menu_add_user_to_group_save)
    void addUserToGroup() {
        // You can specify the ID in the annotation, or use the naming convention

        if (!addDemoUsers.isEmpty()) {
            List<String> ids = new ArrayList<>();
            for (DemoUser demoUser : addDemoUsers) {
                ids.add(demoUser.getUsername());
            }
            final Activity self = this;
            chatGroup.inviteUsers(ids, new EngineCallback<String>() {
                @Override
                public void onSuccess(String s) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(self, GroupSettingActivity_.class);
                            intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET, chatGroup.getGroupId());
                            intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE, ChatType.GroupChat);
                            startActivity(intent);
                            finish();
                        }
                    });

                }

                @Override
                public void onError(ResponseError error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });

                }
            });
        } else {
            back();
        }
    }

}
