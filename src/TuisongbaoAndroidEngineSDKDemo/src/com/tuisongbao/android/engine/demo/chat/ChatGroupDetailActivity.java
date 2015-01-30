package com.tuisongbao.android.engine.demo.chat;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.adapter.ChatGroupAdapter;
import com.tuisongbao.android.engine.demo.chat.adapter.ChatGroupDetailAdapter;

public class ChatGroupDetailActivity extends Activity {

    private ListView mListViewGroupDetail;
    private ChatGroupDetailAdapter mAdapter;
    private List<TSBChatConversation> mListConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        mListViewGroupDetail = (ListView) findViewById(R.id.group_detail_list_view);
        mListConversation = new ArrayList<TSBChatConversation>();

        TSBChatConversation conversation = new TSBChatConversation();
        conversation.setType("send");
        conversation.setTarget("aaaaaaaaa");
        conversation.setLastActiveAt("2014-12-12");
        mListConversation.add(conversation);
        conversation = new TSBChatConversation();
        conversation.setType("reply");
        conversation.setTarget("bbbb");
        conversation.setLastActiveAt("2014-12-13");
        mListConversation.add(conversation);

        mAdapter = new ChatGroupDetailAdapter(mListConversation, this);
        mListViewGroupDetail.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.group_member) {
            Intent intent = new Intent(this, ChatGroupMemberActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }
}
