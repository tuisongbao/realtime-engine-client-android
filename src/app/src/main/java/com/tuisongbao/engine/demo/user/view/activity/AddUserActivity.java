package com.tuisongbao.engine.demo.user.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.common.utils.NetClient;
import com.tuisongbao.engine.demo.common.utils.Utils;
import com.tuisongbao.engine.demo.common.view.activity.BaseActivity;
import com.tuisongbao.engine.demo.conversation.view.activity.ChatConversationActivity;
import com.tuisongbao.engine.demo.conversation.view.activity.ChatConversationActivity_;
import com.tuisongbao.engine.demo.user.adapter.DemoUserAdapter;
import com.tuisongbao.engine.demo.user.entity.DemoUser;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 15-9-6.
 */
@EActivity(R.layout.activity_add_user)
public class AddUserActivity extends BaseActivity {
    @ViewById(R.id.activity_addUser_search_listView)
    ListView adduserList;

    DemoUserAdapter userAdapter;

    @ViewById(R.id.search_user)
    TextView search;


    @ViewById(R.id.txt_right)
    TextView txt_right;

    @ViewById(R.id.img_back)
    ImageView img_back;


    @ViewById(R.id.txt_title)
    TextView txt_title;

    private NetClient netClient;

    private List<DemoUser> demoUsers;

    private Integer position;

    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        netClient = new NetClient(this);
        activity = this;
    }

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

    void searchUser(String username) {
        RequestParams params = new RequestParams();
        params.put("username", username);
        netClient.get(Constants.DEMOUSERURL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Gson gson = new Gson();
                final List<DemoUser> demoUserList = gson.fromJson(new String(responseBody), new TypeToken<List<DemoUser>>() {
                }.getType());

                LogUtils.d(demoUserList);

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

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Utils.showShortToast(activity, "查找用户失败");
            }
        });
    }

    @Click(R.id.txt_right)
    void gotoConversation() {
        if (position == null) {
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
    void choseUser(int position) {
        this.position = position;
        if (demoUsers.get(position).getChecked() != null && demoUsers.get(position).getChecked()) {
            demoUsers.get(position).setChecked(false);
            userAdapter.refresh(demoUsers);
            this.position = null;
            return;
        }
        for (DemoUser user : demoUsers) {
            user.setChecked(false);
        }
        demoUsers.get(position).setChecked(true);
        userAdapter.refresh(demoUsers);
    }

    @Click(R.id.img_back)
    void back() {
        Utils.finish(AddUserActivity.this);
    }
}
