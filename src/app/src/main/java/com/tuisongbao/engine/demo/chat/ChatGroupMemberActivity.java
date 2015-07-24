package com.tuisongbao.engine.demo.chat;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.tuisongbao.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.engine.chat.entity.TSBContactsUser;
import com.tuisongbao.engine.common.TSBEngineCallback;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.chat.adapter.ChatGroupUserAdapter;

public class ChatGroupMemberActivity extends Activity {

    public static final String EXTRA_KEY_GROUP = "com.tuisongbao.android.engine.demo.chat.ChatGroupMemberActivity.EXTRA_KEY_GROUP";

    private TSBChatGroup mGroup;
    private ListView mListViewGroupUser;
    private ChatGroupUserAdapter mAdapter;
    private List<TSBContactsUser> mListGroupUser;
    private Button mButtonQuit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member);
        mListViewGroupUser = (ListView) findViewById(R.id.group_member_list_view);
        mListGroupUser = new ArrayList<TSBContactsUser>();
        mButtonQuit = (Button) findViewById(R.id.group_member_quit);
        mGroup = getIntent().getParcelableExtra(EXTRA_KEY_GROUP);

        mAdapter = new ChatGroupUserAdapter(mListGroupUser, this);
        mListViewGroupUser.setAdapter(mAdapter);
        mListViewGroupUser.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                    final int arg2, long arg3) {
                        new AlertDialog.Builder(ChatGroupMemberActivity.this)
                                .setTitle("确定删除该用户吗？")
                                .setPositiveButton("确定", new OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                        List<String> list = new ArrayList<String>();
                                        list.add(mListGroupUser.get(arg2)
                                                .getUserId());
                                        deleteUser(list);
                                    }
                                })
                                .setNegativeButton("取消", new OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                        // empty

                                    }
                                }).show();
                return true;
            }
        });
        mButtonQuit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                quit();
            }
        });
        request();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_member, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.group_member_add) {
            Intent intent = new Intent(this, ChatGroupMemberAddActivity.class);
            intent.putExtra(ChatGroupMemberAddActivity.EXTRA_KEY_GROUP, mGroup);
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        request();
    }

    private void deleteUser(List<String> list) {
        mGroup.removeUsers(list, new TSBEngineCallback<String>() {

            @Override
            public void onSuccess(String t) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ChatGroupMemberActivity.this, "删除成功", Toast.LENGTH_LONG).show();
                        request();
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ChatGroupMemberActivity.this, "删除失败", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void quit() {
        mGroup.leave(new TSBEngineCallback<String>() {

            @Override
            public void onSuccess(String t) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ChatGroupMemberActivity.this, "你已退出该群", Toast.LENGTH_LONG).show();
                        request();
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ChatGroupMemberActivity.this, "退出失败", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void request() {
        mGroup.getUsers(new TSBEngineCallback<List<TSBContactsUser>>() {

            @Override
            public void onSuccess(List<TSBContactsUser> t) {
                mListGroupUser = t;
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mAdapter.refresh(mListGroupUser);
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ChatGroupMemberActivity.this, "获取成员列表失败，请稍后再试", Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
    }
}
