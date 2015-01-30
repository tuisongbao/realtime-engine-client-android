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
import android.widget.TextView;

import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupUser;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.adapter.ChatGroupAdapter;
import com.tuisongbao.android.engine.demo.chat.adapter.ChatGroupDetailAdapter;
import com.tuisongbao.android.engine.demo.chat.adapter.ChatGroupUserAdapter;

public class ChatGroupMemberAddActivity extends Activity {

    private ListView mListViewGroupUser;
    private ChatGroupUserAdapter mAdapter;
    private List<TSBChatGroupUser> mListGroupUser;
    private TextView mTextViewQuit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member);
        mListViewGroupUser = (ListView) findViewById(R.id.group_member_list_view);
        mListGroupUser = new ArrayList<TSBChatGroupUser>();
        mTextViewQuit = (TextView) findViewById(R.id.group_member_quit);

        TSBChatGroupUser groupMember = new TSBChatGroupUser();
        groupMember.setUserId("用户A");
        mListGroupUser.add(groupMember);
        groupMember = new TSBChatGroupUser();
        groupMember.setUserId("用户B");
        mListGroupUser.add(groupMember);

        mAdapter = new ChatGroupUserAdapter(mListGroupUser, this);
        mListViewGroupUser.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_member, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.group_member_add) {
            
            return true;
        }
        return false;
    }
}
