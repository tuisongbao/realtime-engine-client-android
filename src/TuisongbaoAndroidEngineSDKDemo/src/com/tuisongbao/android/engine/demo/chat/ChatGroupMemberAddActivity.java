package com.tuisongbao.android.engine.demo.chat;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.util.StrUtil;

public class ChatGroupMemberAddActivity extends Activity {

    public static final String TAG = "com.tuisongbao.android.engine.demo.chat.ChatGroupMemberActivity";
    public static final String EXTRA_KEY_GROUP = "com.tuisongbao.android.engine.demo.chat.ChatGroupMemberActivity.EXTRA_KEY_GROUP";

    private TSBChatGroup mGroup;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member_add);
        mEditText = (EditText) findViewById(R.id.group_member_add_edittext);
        mGroup = getIntent().getParcelableExtra(EXTRA_KEY_GROUP);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_member_add, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.group_member_add_finish) {
            inviteMembers();
            return true;
        }
        return false;
    }

    private void inviteMembers() {
        String str = mEditText.getText().toString();
        if (StrUtil.isEmpty(str)) {
            Toast.makeText(this, "请输入用户id", Toast.LENGTH_LONG).show();
        } else {
            List<String> list = new ArrayList<String>();
            String[] splits = str.split(",");
            for (String split : splits) {
                list.add(split);
            }
            mGroup.joinInvitation(list, new TSBEngineCallback<String>() {

                @Override
                public void onSuccess(String t) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(ChatGroupMemberAddActivity.this, "添加成功", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onError(int code, String message) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(ChatGroupMemberAddActivity.this, "添加失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
    }
}
