package com.tuisongbao.android.engine.demo.chat;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.tuisongbao.android.engine.demo.R;

public class ChatGroupMemberAddActivity extends Activity {

    private ListView mListViewMemberAdd;
    private EditText mEditText;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member_add);
        mListViewMemberAdd = (ListView) findViewById(R.id.group_member_add_listview);
        mEditText = (EditText) findViewById(R.id.group_member_add_edittext);
        mButton = (Button) findViewById(R.id.group_member_add_button);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_member_add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.group_member_add_finish) {

            return true;
        }
        return false;
    }
}
