package com.tuisongbao.android.engine.demo.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.tuisongbao.android.engine.chat.entity.ChatType;
import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.chat.entity.TSBImageMessageBody;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.entity.TSBMessageBody;
import com.tuisongbao.android.engine.chat.entity.TSBTextMessageBody;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.adapter.ChatMessagesAdapter;
import com.tuisongbao.android.engine.demo.chat.cache.LoginChache;
import com.tuisongbao.android.engine.demo.chat.service.TSBMessageRevieveService;

@SuppressLint("NewApi")
public class ChatConversationActivity extends Activity implements LoaderCallbacks<Cursor> {

    public static final String EXTRA_CONVERSATION = "com.tuisongbao.android.engine.demo.chat.ChatConversationActivity.EXTRA_CONVERSATION";
    private static final String TAG = "com.tuisongbao.android.engine.demo.ChatConversationActivity";

    private ListView mMessagesListView;
    private Button mSendButton;
    private Button mImageSelectButton;
    private EditText mContenEditText;
    private ChatMessagesAdapter mAdapter;
    private List<TSBMessage> mMessageList;
    private TSBChatConversation mConversation;

    private Uri mImageUri = null;
    private Long mStartMessageId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        mMessagesListView = (ListView) findViewById(R.id.conversation_messages_list_view);
        mSendButton = (Button) findViewById(R.id.conversation_text_send_button);
        mImageSelectButton = (Button) findViewById(R.id.conversation_media_send_button);
        mContenEditText = (EditText) findViewById(R.id.conversation_message_content_edittext);
        mMessageList = new ArrayList<TSBMessage>();
        mConversation = getIntent().getParcelableExtra(EXTRA_CONVERSATION);

        mAdapter = new ChatMessagesAdapter(mMessageList, this);
        mMessagesListView.setAdapter(mAdapter);
        mSendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                TSBMessageBody body = new TSBTextMessageBody(mContenEditText.getText().toString());
                mConversation.sendMessage(body, new TSBEngineCallback<TSBMessage>() {

                    @Override
                    public void onSuccess(TSBMessage t) {
                        t.setFrom(LoginChache.getUserId());
                        mMessageList.add(t);
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mAdapter.refresh(mMessageList);
                                mMessagesListView.setSelection(mMessageList.size() - 1);
                                Toast.makeText(ChatConversationActivity.this, "Send success", Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                    @Override
                    public void onError(final int code, final String message) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(ChatConversationActivity.this, "Send failure [code=" + code + ";msg=" + message + "]", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                mContenEditText.setText("");
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                      // imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
                if (imm.isActive()) // 一直是true
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
                            InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        // Select a image
        mImageSelectButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            }
        });

        mMessagesListView.setOnScrollListener(new OnScrollListener() {

            int currentFirstVisibleItem;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (currentFirstVisibleItem == 0 && scrollState == SCROLL_STATE_IDLE) {
                    request();
                }
            }

            @Override
            public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                this.currentFirstVisibleItem = firstVisibleItem;
            }
        });

        registerBroadcast();

        // Request the latest messages.
        request();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            mImageUri = data.getData();
            // Query the real path of the image.
            getLoaderManager().restartLoader(0, null, this);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mConversation.getType() == ChatType.GroupChat) {
            getMenuInflater().inflate(R.menu.group_detail, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.group_member) {
            TSBChatGroup group = new TSBChatGroup();
            group.setGroupId(mConversation.getTarget());

            Intent intent = new Intent(this, ChatGroupMemberActivity.class);
            intent.putExtra(ChatGroupMemberActivity.EXTRA_KEY_GROUP, group);
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

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return getAppropriateLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        handlerLoaderFinished(arg1);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub

    }

    private Loader<Cursor> getAppropriateLoader() {
        // The Uri of different Android version has different format.
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion > android.os.Build.VERSION_CODES.KITKAT) {
            // If the SDK version is over lollipop, have to query all
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE };

            CursorLoader cursorLoader = new CursorLoader(ChatConversationActivity.this, uri, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " desc");
            return cursorLoader;

        } else {
            String[] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.LATITUDE, MediaStore.Images.Media.LONGITUDE };

            CursorLoader cursorLoader = new CursorLoader(ChatConversationActivity.this, mImageUri, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " desc");
            return cursorLoader;
        }
    }

    private void handlerLoaderFinished(Cursor cursor) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        String realPath = "";
        if (currentapiVersion > android.os.Build.VERSION_CODES.KITKAT){
            String wholeID = DocumentsContract.getDocumentId(mImageUri);
            Log.d(TAG, wholeID);
            // Lollipop: content://com...../media:{id}, match id to find the file path.
            String[] splits = wholeID.split(":");

            if (splits.length != 2) {
                return;
            }
            String imageId = splits[1];
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns._ID));

                if (imageId.equals(id)) {
                    realPath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                    break;
                }
                cursor.moveToNext();
            }
        } else {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                realPath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            }
        }
        sendImageMessage(realPath);
    }

    private void request() {
        mConversation.getMessages(mStartMessageId, null, 20, new TSBEngineCallback<List<TSBMessage>>() {

            @Override
            public void onSuccess(final List<TSBMessage> t) {
                Log.d(TAG, "Get " + t.size() + " messages");
                if (t.size() < 1) {
                    return;
                }
                mStartMessageId = t.get(0).getMessageId();
                mStartMessageId = mStartMessageId - Long.valueOf(t.size());
                Collections.reverse(t);
                t.addAll(mMessageList);
                mMessageList = t;

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mAdapter.refresh(mMessageList);
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(ChatConversationActivity.this, "获取消息失败，请稍后再试", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void sendImageMessage(String filePath) {
        if (filePath == null || filePath.length() < 1) {
            Toast.makeText(ChatConversationActivity.this, "Send failed, file not exist", Toast.LENGTH_LONG).show();
            return;
        }
        TSBImageMessageBody body = new TSBImageMessageBody();
        body.setLocalPath(filePath);
        mConversation.sendMessage(body, new TSBEngineCallback<TSBMessage>() {

            @Override
            public void onSuccess(final TSBMessage t) {
                mMessageList.add(t);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mAdapter.refresh(mMessageList);
                        mMessagesListView.setSelection(mMessageList.size() - 1);
                        Toast.makeText(ChatConversationActivity.this, "Send success", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mAdapter.refresh(mMessageList);
                        mMessagesListView.setSelection(mMessageList.size() - 1);
                        Toast.makeText(ChatConversationActivity.this, "Send failed", Toast.LENGTH_LONG).show();
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
                String target = mConversation.getTarget();
                TSBMessage message = intent.getParcelableExtra(TSBMessageRevieveService.BROADCAST_EXTRA_KEY_MESSAGE);
                Log.i(TAG, "App get " + message.toString() + " to " + message.getRecipient() + " and corrent target is " + target);

                // Only receive message sent to current conversation
                boolean showMessage = false;
                if (message.getChatType() == ChatType.SingleChat) {
                    showMessage = message.getFrom().equals(target);
                } else if (message.getChatType() == ChatType.GroupChat) {
                    showMessage = message.getRecipient().equals(target);
                }
                if (message != null && showMessage) {
                    mMessageList.add(message);
                    mAdapter.refresh(mMessageList);
                    mMessagesListView.setSelection(mMessageList.size() - 1);
                }
            }
        }
    };
}
