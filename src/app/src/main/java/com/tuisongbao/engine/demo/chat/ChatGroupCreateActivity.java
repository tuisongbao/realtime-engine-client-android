package com.tuisongbao.engine.demo.chat;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.tuisongbao.engine.chat.group.entity.ChatGroup;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.DemoApplication;
import com.tuisongbao.engine.demo.R;

import java.util.ArrayList;
import java.util.List;

public class ChatGroupCreateActivity extends Activity {

    private EditText mGroupNameEditText;
    private EditText mGroupDescEditText;
    private ToggleButton mGroupIsPrivateToggleButton;
    private ToggleButton mGroupInvitedPermissionToggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);
        mGroupNameEditText = (EditText) findViewById(R.id.group_create_name);
        mGroupDescEditText = (EditText) findViewById(R.id.group_create_detail);
        mGroupIsPrivateToggleButton = (ToggleButton) findViewById(R.id.group_create_detail_is_private);
        mGroupInvitedPermissionToggleButton = (ToggleButton) findViewById(R.id.group_create_detail_invited_permission);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_create, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.group_finish) {
            create();
            return true;
        }
        return false;
    }

    private void create() {
        List<String> members = new ArrayList<String>();

        DemoApplication.getGroupManager().create(members,
                !mGroupIsPrivateToggleButton.isChecked(),
                !mGroupInvitedPermissionToggleButton.isChecked(),
                new EngineCallback<ChatGroup>() {

                    @Override
                    public void onSuccess(ChatGroup t) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(ChatGroupCreateActivity.this,
                                        "群组创建成功", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onError(ResponseError error) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(ChatGroupCreateActivity.this,
                                        "群组创建失败，请稍后再试", Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }
                });
    }

}
