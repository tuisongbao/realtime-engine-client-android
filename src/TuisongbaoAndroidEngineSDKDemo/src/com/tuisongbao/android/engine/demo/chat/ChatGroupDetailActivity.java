package com.tuisongbao.android.engine.demo.chat;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.entity.ChatType;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.entity.TSBMessageBody;
import com.tuisongbao.android.engine.chat.entity.TSBTextMessageBody;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.adapter.ChatGroupDetailAdapter;
import com.tuisongbao.android.engine.demo.chat.service.TSBMessageRevieveService;
import com.tuisongbao.android.engine.service.TSBChatIntentService;

public class ChatGroupDetailActivity extends Activity {

    public static final String EXTRA_CODE_TARGET = "com.tuisongbao.android.engine.demo.chat.ChatGroupDetailActivity.EXTRA_CODE_TARGET";
    public static final String EXTRA_CODE_CHAT_TYPE = "com.tuisongbao.android.engine.demo.chat.ChatGroupDetailActivity.EXTRA_CODE_CHAT_TYPE";
    private String mTarget;
    private ChatType mChatType;
    private ListView mListViewGroupDetail;
    private Button mSendButton;
    private EditText mContenEditText;
    private ChatGroupDetailAdapter mAdapter;
    private List<TSBMessage> mListConversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        mListViewGroupDetail = (ListView) findViewById(R.id.group_detail_list_view);
        mSendButton = (Button) findViewById(R.id.group_detail_send_button);
        mContenEditText = (EditText) findViewById(R.id.group_detail_message_content_edittext);
        mListConversation = new ArrayList<TSBMessage>();
        mTarget = getIntent().getStringExtra(EXTRA_CODE_TARGET);
        mChatType = ChatType.getType(getIntent().getStringExtra(EXTRA_CODE_CHAT_TYPE));

        mAdapter = new ChatGroupDetailAdapter(mListConversation, this);
        mListViewGroupDetail.setAdapter(mAdapter);
        mSendButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                TSBMessage message = TSBMessage.createMessage(TSBMessage.TYPE.TEXT);
                TSBMessageBody body = new TSBTextMessageBody(mContenEditText.getText().toString());
                message.setBody(body).setChatType(mChatType).setRecipient(mTarget);
                TSBChatManager.getInstance().sendMessage(message, new TSBEngineCallback<TSBMessage>() {
                    
                    @Override
                    public void onSuccess(TSBMessage t) {
                        mListConversation.add(t);
                        runOnUiThread(new Runnable() {
                            
                            @Override
                            public void run() {
                                mAdapter.refresh(mListConversation);
                                Toast.makeText(ChatGroupDetailActivity.this, "Send success", Toast.LENGTH_LONG).show();
                            }
                        });
                        
                    }
                    
                    @Override
                    public void onError(final int code, final String message) {
                        runOnUiThread(new Runnable() {
                            
                            @Override
                            public void run() {
                                Toast.makeText(ChatGroupDetailActivity.this, "Send failure [code=" + code + ";msg=" + message + "]", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        });
        registerBroadcast();
        request();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mChatType == ChatType.GroupChat) {
            getMenuInflater().inflate(R.menu.group_detail, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.group_member) {
            Intent intent = new Intent(this, ChatGroupMemberActivity.class);
            intent.putExtra(ChatGroupMemberActivity.EXTRA_KEY_GROUP_ID, mTarget);
            startActivity(intent);
            return true;
        }
        return false;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
    }
    
    private void request() {
        TSBChatManager.getInstance().getMessages(mChatType, mTarget, 0, 1000, 20, new TSBEngineCallback<List<TSBMessage>>() {
            
            @Override
            public void onSuccess(List<TSBMessage> t) {
                mListConversation = t;
                runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        mAdapter.refresh(mListConversation);
                    }
                });
            }
            
            @Override
            public void onError(int code, String message) {
                runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        Toast.makeText(ChatGroupDetailActivity.this, "获取消息失败，请稍后再试", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    
    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();  
        filter.addAction(TSBMessageRevieveService.BROADCAST_ACTION_RECEIVED_MESSAGE);  
        registerReceiver(mBroadcastReceiver, filter); 
    }
    
    private void unregisterBroadcast() {
        unregisterReceiver(mBroadcastReceiver);
    }
    
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TSBMessageRevieveService.BROADCAST_ACTION_RECEIVED_MESSAGE.equals(intent.getAction())) {
                TSBMessage message = intent.getParcelableExtra(TSBMessageRevieveService.BROADCAST_EXTRA_KEY_MESSAGE);
                if (message != null) {
                    mListConversation.add(message);
                    mAdapter.refresh(mListConversation);
                }
            }
        }
    };
}
