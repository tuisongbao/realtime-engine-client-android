package com.tuisongbao.engine.demo.chat;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.tuisongbao.engine.chat.group.entity.ChatGroup;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.DemoApplication;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.utils.StrUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatGroupMemberAddActivity extends Activity {

    public static final String TAG = "TSB" + "TSB" + "com.tuisongbao.android.engine.demo.chat.ChatGroupMemberActivity";
    public static final String EXTRA_KEY_GROUP = "com.tuisongbao.android.engine.demo.chat.ChatGroupMemberActivity.EXTRA_KEY_GROUP";

    private ChatGroup mGroup;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member_add);
        mEditText = (EditText) findViewById(R.id.group_member_add_edittext);
        String groupString = getIntent().getStringExtra(EXTRA_KEY_GROUP);
        mGroup = ChatGroup.deserialize(DemoApplication.engine, groupString);
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
        if (StrUtils.isEmpty(str)) {
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
                public void onError(ResponseError error) {
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
