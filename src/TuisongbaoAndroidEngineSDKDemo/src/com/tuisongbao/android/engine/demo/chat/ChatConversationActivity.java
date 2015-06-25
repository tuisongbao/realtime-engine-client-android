package com.tuisongbao.android.engine.demo.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.tuisongbao.android.engine.chat.entity.ChatType;
import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.chat.entity.TSBImageMessageBody;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.entity.TSBMessageBody;
import com.tuisongbao.android.engine.chat.entity.TSBTextMessageBody;
import com.tuisongbao.android.engine.chat.entity.TSBVoiceMessageBody;
import com.tuisongbao.android.engine.chat.media.TSBMediaPlayer;
import com.tuisongbao.android.engine.chat.media.TSBMediaRecorder;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.adapter.ChatMessagesAdapter;
import com.tuisongbao.android.engine.demo.chat.cache.LoginChache;
import com.tuisongbao.android.engine.demo.chat.service.TSBMessageRevieveService;
import com.tuisongbao.android.engine.log.LogUtil;

@SuppressLint("NewApi")
public class ChatConversationActivity extends Activity implements
        LoaderCallbacks<Cursor> {
    enum UriType {
        TYPE1, // content://com...../medihideSoftKeyboarda:{id}
        TYPE2 // content://media/external/images/media/32114
    }

    public final static String BROADCAST_ACTION_MESSAGE_SENT = "com.tuisongbao.android.engine.demo.ChatConversationActivity.MessageSent";
    public final static String BROADCAST_EXTRA_KEY_MESSAGE = "com.tuisongbao.android.engine.demo.ChatConversationActivity.ExtraMessage";
    public static final String EXTRA_CONVERSATION = "com.tuisongbao.android.engine.demo.chat.ChatConversationActivity.EXTRA_CONVERSATION";
    private static final String TAG = "com.tuisongbao.android.engine.demo.ChatConversationActivity";

    private static final int REQUEST_CODE_IMAGE = 1;
    private static final int REQUEST_CODE_TAKE_VIDEO = 2;

    private ListView mMessagesListView;
    private Button mSendButton;
    private Button mMoreButton;
    private Button mVoiceTextSwitchButton;
    private Button mVoiceRecorderButton;
    private EditText mContentEditText;
    private ChatMessagesAdapter mMessagesAdapter;
    private List<TSBMessage> mMessageList;
    private TSBChatConversation mConversation;

    private LinearLayout mMediaMessageOptionsLayout;

    private Uri mImageUri = null;
    private Long mStartMessageId = null;
    private TSBMediaRecorder mRecorder;
    private long mRecordStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        mConversation = getIntent().getParcelableExtra(EXTRA_CONVERSATION);

        mMessagesListView = (ListView) findViewById(R.id.conversation_messages_list_view);
        mContentEditText = (EditText) findViewById(R.id.conversation_message_content_edittext);
        mSendButton = (Button) findViewById(R.id.conversation_text_send_button);
        mMoreButton = (Button) findViewById(R.id.conversation_more_send_button);
        mVoiceTextSwitchButton = (Button) findViewById(R.id.conversation_voice_text_switch_button);
        mVoiceRecorderButton = (Button) findViewById(R.id.conversation_voice_recorder_button);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        mContentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int start, int before,
                    int count) {
                // start位置的before个字符变成count个
                if (start + count > 0) {
                    // Show send button
                    mSendButton.setVisibility(View.VISIBLE);
                    mMoreButton.setVisibility(View.GONE);

                } else {
                    // Show more button
                    mSendButton.setVisibility(View.GONE);
                    mMoreButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int start,
                    int count, int after) {
                // start位置的count个字符变成after个
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        mMessageList = new ArrayList<TSBMessage>();
        mMessagesAdapter = new ChatMessagesAdapter(mMessageList, this);
        mMessagesListView.setAdapter(mMessagesAdapter);

        mSendButton.setVisibility(View.GONE);
        mSendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                TSBMessageBody body = new TSBTextMessageBody(mContentEditText
                        .getText().toString());
                mConversation.sendMessage(body,
                        new TSBEngineCallback<TSBMessage>() {

                            @Override
                            public void onSuccess(TSBMessage t) {
                                t.setFrom(LoginChache.getUserId());
                                mMessageList.add(t);

                                Intent intent = new Intent(
                                        BROADCAST_ACTION_MESSAGE_SENT);
                                intent.putExtra(BROADCAST_EXTRA_KEY_MESSAGE, t);
                                sendBroadcast(intent);

                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        mMessagesAdapter.refresh(mMessageList);
                                        mMessagesListView
                                                .setSelection(mMessageList
                                                        .size() - 1);
                                        Toast.makeText(
                                                ChatConversationActivity.this,
                                                "Send success",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });

                            }

                            @Override
                            public void onError(final int code,
                                    final String message) {
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        Toast.makeText(
                                                ChatConversationActivity.this,
                                                "Send failure [code=" + code
                                                        + ";msg=" + message
                                                        + "]",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                mContentEditText.setText("");
                hideSoftKeyboard();
            }
        });

        // Select a image
        Button imageSelectButton = (Button) findViewById(R.id.conversation_message_create_image);
        imageSelectButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_CODE_IMAGE);
            }
        });

        Button videoRecordButton = (Button) findViewById(R.id.conversation_message_create_video);
        videoRecordButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.d(TAG, "Show camera");
                Intent intent = new Intent();
                intent.setAction("com.tuisongbao.android.engine.media.TSBMediaRecorder.INTENT_ACTION_TAKE_VIDEO");
                startActivityForResult(intent, REQUEST_CODE_TAKE_VIDEO);
            }
        });

        mMoreButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                hideSoftKeyboard();
                mMediaMessageOptionsLayout = (LinearLayout) findViewById(R.id.conversation_message_create_multi);
                int visiable = mMediaMessageOptionsLayout.getVisibility();
                if (visiable == View.GONE) {
                    mMediaMessageOptionsLayout.setVisibility(View.VISIBLE);
                } else if (visiable == View.VISIBLE) {
                    mMediaMessageOptionsLayout.setVisibility(View.GONE);
                }
            }
        });

        mVoiceTextSwitchButton.setText("语音");
        mVoiceTextSwitchButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (mMediaMessageOptionsLayout != null) {
                    mMediaMessageOptionsLayout.setVisibility(View.GONE);
                }
                if (mVoiceTextSwitchButton.getText().equals("文本")) {
                    mVoiceTextSwitchButton.setText("语音");

                    mContentEditText.setVisibility(View.VISIBLE);
                    mVoiceRecorderButton.setVisibility(View.GONE);

                    // Switch to send button if the edit text is not empty.
                    mContentEditText.requestFocus();
                    showSoftKeyboard();

                    if (mContentEditText.getText().toString().length() > 0) {
                        mSendButton.setVisibility(View.VISIBLE);
                        mMoreButton.setVisibility(View.GONE);
                    }
                } else {
                    mVoiceTextSwitchButton.setText("文本");
                    mContentEditText.setVisibility(View.GONE);
                    mVoiceRecorderButton.setVisibility(View.VISIBLE);

                    // Show more button
                    mSendButton.setVisibility(View.GONE);
                    mMoreButton.setVisibility(View.VISIBLE);

                    hideSoftKeyboard();
                }

            }
        });

        mVoiceRecorderButton.setVisibility(View.GONE);
        mVoiceRecorderButton.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                int actionCode = event.getActionMasked();
                if (actionCode == MotionEvent.ACTION_DOWN) {
                    // Press record button
                    mRecordStartTime = new Date().getTime();
                    onRecordStart();
                    mVoiceRecorderButton.setText("松开 发送");
                    return true;

                } else if (actionCode == MotionEvent.ACTION_UP) {
                    // Release record button
                    long recordEndTime = new Date().getTime();
                    if (recordEndTime - mRecordStartTime > 2000) {

                        Object tag = mVoiceRecorderButton.getTag();
                        // User has cancel this operation
                        if (tag != null && tag.equals("cancel")) {
                            LogUtil.info(TAG,
                                    "Voice operation has been canceled");
                            mRecorder.cancel();
                        } else {
                            onRecordFinished();
                        }
                    } else {
                        // if the duration is shorter than 2 seconds, ignore
                        // this operation.
                        mRecorder.cancel();
                    }
                    mVoiceRecorderButton.setTag("normal");
                    mVoiceRecorderButton.setText("按住 说话");

                    return true;
                } else {
                    // Moving up by 150px will cancel sending voice message
                    if (event.getY() < -150) {
                        mVoiceRecorderButton.setTag("cancel");
                        mVoiceRecorderButton.setText("松开手指 取消发送");
                    } else {
                        mVoiceRecorderButton.setTag("normal");
                        mVoiceRecorderButton.setText("松开 发送");
                    }
                }
                return false;
            }
        });

        mMessagesListView.setOnScrollListener(new OnScrollListener() {

            int currentFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (currentFirstVisibleItem == 0
                        && scrollState == SCROLL_STATE_IDLE) {
                    request();
                }
            }

            @Override
            public void onScroll(AbsListView arg0, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
                this.currentFirstVisibleItem = firstVisibleItem;
            }
        });

        registerBroadcast();

        // Request the latest messages.
        request();

        mRecorder = new TSBMediaRecorder();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == RESULT_OK) {
            mImageUri = intent.getData();
            LogUtil.debug(TAG, "mImageUri" + mImageUri.getPath() + "  " + mImageUri);
            // Query the real path of the image.
            getLoaderManager().restartLoader(0, null, this);
        } else if (requestCode == REQUEST_CODE_TAKE_VIDEO && resultCode == RESULT_OK) {
            // TODO: parse video from data
            Uri videoUri = intent.getData();
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mConversation.getType() == ChatType.GroupChat) {
            getMenuInflater().inflate(R.menu.group_detail, menu);
            getActionBar().setTitle(mConversation.getGroupName());
        } else {
            getActionBar().setTitle(mConversation.getTarget());
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
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        // The Uri of different Android version has different format.
        if (getUriType(mImageUri) == UriType.TYPE1) {
            // Query all, then filter by ID when loader finished
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] projection = { MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.LATITUDE,
                    MediaStore.Images.Media.LONGITUDE };

            CursorLoader cursorLoader = new CursorLoader(
                    ChatConversationActivity.this, uri, projection, null, null,
                    MediaStore.Images.Media.DATE_ADDED + " desc");
            return cursorLoader;

        } else {
            String[] projection = { MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.LATITUDE,
                    MediaStore.Images.Media.LONGITUDE };

            CursorLoader cursorLoader = new CursorLoader(
                    ChatConversationActivity.this, mImageUri, projection, null,
                    null, MediaStore.Images.Media.DATE_ADDED + " desc");
            return cursorLoader;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        try {
            String realPath = "";

            if (getUriType(mImageUri) == UriType.TYPE1) {
                String [] splits = mImageUri.getPath().split(":");
                String imageId = splits[1];
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    String id = cursor.getString(cursor
                            .getColumnIndex(MediaStore.MediaColumns._ID));

                    if (imageId.equals(id)) {
                        realPath = cursor.getString(cursor
                                .getColumnIndex(MediaStore.MediaColumns.DATA));
                        break;
                    }
                    cursor.moveToNext();
                }
            } else {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    realPath = cursor.getString(cursor
                            .getColumnIndex(MediaStore.MediaColumns.DATA));
                }
            }
            sendImageMessage(realPath);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        TSBMediaPlayer.getInstance().stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
    }

    private void onRecordStart() {
        LogUtil.info(TAG, "Recording.....");
        mRecorder.start();
    }

    private void onRecordFinished() {
        LogUtil.info(TAG, "Record finished");
        String filePath = mRecorder.stop();

        onVoiceMessageSent(filePath);
    }

    private void onVoiceMessageSent(String filePath) {
        TSBVoiceMessageBody body = new TSBVoiceMessageBody();
        body.setLocalPath(filePath);
        mConversation.sendMessage(body, new TSBEngineCallback<TSBMessage>() {

            @Override
            public void onSuccess(final TSBMessage t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMessageList.add(t);
                        mMessagesAdapter.refresh(mMessageList);
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void request() {
        mConversation.getMessages(mStartMessageId, null, 20,
                new TSBEngineCallback<List<TSBMessage>>() {

                    @Override
                    public void onSuccess(final List<TSBMessage> t) {
                        Log.d(TAG, "Get " + t.size() + " messages");
                        if (t.size() < 1) {
                            return;
                        }
                        final int selectionPosition = t.size();
                        mStartMessageId = t.get(0).getMessageId();
                        mStartMessageId = mStartMessageId
                                - Long.valueOf(t.size());
                        Collections.reverse(t);
                        t.addAll(mMessageList);
                        mMessageList = t;

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mMessagesAdapter.refresh(mMessageList);
                                mMessagesListView
                                        .setSelection(selectionPosition);
                            }
                        });
                    }

                    @Override
                    public void onError(int code, String message) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(ChatConversationActivity.this,
                                        "获取消息失败，请稍后再试", Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }
                });
    }

    private void sendImageMessage(String filePath) {
        if (filePath == null || filePath.length() < 1) {
            Toast.makeText(ChatConversationActivity.this,
                    "Send failed, file not exist", Toast.LENGTH_LONG).show();
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
                        mMessagesAdapter.refresh(mMessageList);
                        mMessagesListView.setSelection(mMessageList.size() - 1);
                        Toast.makeText(ChatConversationActivity.this,
                                "Send success", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mMessagesAdapter.refresh(mMessageList);
                        mMessagesListView.setSelection(mMessageList.size() - 1);
                        Toast.makeText(ChatConversationActivity.this,
                                "Send failed", Toast.LENGTH_LONG).show();
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
            if (TSBMessageRevieveService.BROADCAST_ACTION_RECEIVED_MESSAGE
                    .equals(intent.getAction())) {
                String target = mConversation.getTarget();
                TSBMessage message = intent
                        .getParcelableExtra(TSBMessageRevieveService.BROADCAST_EXTRA_KEY_MESSAGE);
                Log.i(TAG,
                        "App get " + message.toString() + " to "
                                + message.getRecipient()
                                + " and corrent target is " + target);

                // Only receive message sent to current conversation
                boolean showMessage = false;
                if (message.getChatType() == ChatType.SingleChat) {
                    showMessage = message.getFrom().equals(target);
                } else if (message.getChatType() == ChatType.GroupChat) {
                    showMessage = message.getRecipient().equals(target);
                }
                if (message != null && showMessage) {
                    mMessageList.add(message);
                    mMessagesAdapter.refresh(mMessageList);
                    mMessagesListView.setSelection(mMessageList.size() - 1);
                }
            }
        }
    };

    private void showSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mContentEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideSoftKeyboard() {
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(getCurrentFocus()
                .getApplicationWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /***
     * Different android system may have different format of Uri. See UriType for detail.
     * If it is TYPE1, when query real path of the image, have to query all images and then filter them by the id field in Uri
     * If it is TYPE2, can query the real path directly by CursorLoader.
     *
     * See also: onCreateLoader() and onLoadFinished()
     *
     * @param uri
     * @return
     */
    private UriType getUriType(Uri uri) {
        String [] splits = mImageUri.getPath().split(":");
        if (splits.length > 1) {
            return UriType.TYPE1;
        } else {
            return UriType.TYPE2;
        }
    }
}
