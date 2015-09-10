package com.tuisongbao.engine.demo.view.activity;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.adpter.DemoUserAdapter;
import com.tuisongbao.engine.demo.bean.DemoUser;
import com.tuisongbao.engine.demo.chat.ChatConversationActivity;
import com.tuisongbao.engine.demo.chat.ChatConversationActivity_;
import com.tuisongbao.engine.demo.common.Utils;
import com.tuisongbao.engine.demo.service.ChatDemoService;
import com.tuisongbao.engine.demo.view.BaseActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 15-9-6.
 */
@EActivity(R.layout.activity_add_user)
public class AddUser extends BaseActivity{
    @ViewById(R.id.activity_addUser_search_listView)
    ListView adduserList;

    DemoUserAdapter userAdapter;

    @ViewById(R.id.search_user)
    TextView search;


    @ViewById(R.id.txt_right)
    TextView txt_right;

    @RestService
    ChatDemoService userService;

    @ViewById(R.id.img_back)
    ImageView img_back;


    @ViewById(R.id.txt_title)
    TextView txt_title;

    List<DemoUser> demoUsers;

    private Integer position;

    @AfterViews
    public void afterViews() {
        txt_right.setText("确认");
        txt_title.setText("请选择聊天的对象");
        img_back.setVisibility(View.VISIBLE);
        demoUsers = new ArrayList<>();
        userAdapter = new DemoUserAdapter(this, demoUsers);
        adduserList.setAdapter(userAdapter);
    }

    @TextChange(R.id.search_user)
    void onTextChangesOnSearchUserTextView(CharSequence text, TextView searchUserTextView, int before, int start, int count) {
        position = null;
        if ("".equals(text.toString().trim())) {
            demoUsers = new ArrayList<>();
            userAdapter.refresh(demoUsers);
        } else {
            searchUser(text.toString());
        }
    }

    @Background
    void searchUser(String username) {
        String token = App.getInstance2().getToken();
        List<DemoUser> demoUserList = userService.getDemoUser(username, token);
        if (demoUserList != null) {
            demoUsers = demoUserList;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    userAdapter.refresh(demoUsers);
                }
            });
        }
    }

    @Click(R.id.txt_right)
    void gotoConversation() {
        if(position == null){
            Utils.showShortToast(this, "没有选中用户");
            return;
        }

        Intent intent = new Intent(this,
                ChatConversationActivity_.class);
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET, demoUsers.get(position).getUsername());
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE, ChatType.SingleChat);

        startActivity(intent);
    }

    @ItemClick(R.id.activity_addUser_search_listView)
    void choseUser(int position){
        this.position = position;
        if(demoUsers.get(position).getChecked()!=null && demoUsers.get(position).getChecked()){
            demoUsers.get(position).setChecked(false);
            userAdapter.refresh(demoUsers);
            this.position = null;
            return;
        }
        for (DemoUser user: demoUsers){
            user.setChecked(false);
        }
        demoUsers.get(position).setChecked(true);
        userAdapter.refresh(demoUsers);
    }

    @Click(R.id.img_back)
    void back() {
        Utils.finish(AddUser.this);
    }

}
