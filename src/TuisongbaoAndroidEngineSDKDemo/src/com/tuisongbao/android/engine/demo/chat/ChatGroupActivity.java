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

import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.adapter.ChatGroupAdapter;

public class ChatGroupActivity extends Activity {

    private ListView mListViewGroup;
    private ChatGroupAdapter mAdapter;
    private List<TSBChatGroup> mListGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);
        mListViewGroup = (ListView) findViewById(R.id.group_list_view);
        mListGroup = new ArrayList<TSBChatGroup>();
        TSBChatGroup group = new TSBChatGroup();
        group.setName("群组 A");
        mListGroup.add(group);
        group = new TSBChatGroup();
        group.setName("群组 B");
        mListGroup.add(group);
        mAdapter = new ChatGroupAdapter(mListGroup, this);
        mListViewGroup.setAdapter(mAdapter);
        mListViewGroup.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                Intent intent = new Intent(ChatGroupActivity.this,
                        ChatGroupDetailActivity.class);
                startActivity(intent);
            }
        });
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

}
