package com.tuisongbao.android.engine.demo.chat;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.tuisongbao.android.engine.chat.TSBGroupManager;
import com.tuisongbao.android.engine.chat.entity.ChatType;
import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.adapter.ChatGroupAdapter;

public class ChatGroupsActivity extends Activity {

    private ListView mListViewGroup;
    private ChatGroupAdapter mAdapter;
    private List<TSBChatGroup> mListGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        mListViewGroup = (ListView) findViewById(R.id.group_list_view);
        mListGroup = new ArrayList<TSBChatGroup>();
        mAdapter = new ChatGroupAdapter(mListGroup, this);
        mListViewGroup.setAdapter(mAdapter);
        mListViewGroup.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                TSBChatGroup group = mListGroup.get(arg2);

                TSBChatConversation conversation = new TSBChatConversation();
                conversation.setTarget(group.getGroupId());
                conversation.setType(ChatType.GroupChat);

                Intent intent = new Intent(ChatGroupsActivity.this,
                        ChatConversationActivity.class);
                intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION, conversation);
                startActivity(intent);
            }
        });
        request();
    }

    @Override
    protected void onResume() {
        super.onResume();
        request();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.group_add) {
            Intent intent = new Intent(this, ChatGroupCreateActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    private void request() {
        TSBGroupManager.getInstance().getList(null, new TSBEngineCallback<List<TSBChatGroup>>() {

            @Override
            public void onSuccess(List<TSBChatGroup> t) {
                mListGroup  = t;
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mAdapter.refresh(mListGroup);
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ChatGroupsActivity.this, "获取群组失败，请稍后再试", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

}
