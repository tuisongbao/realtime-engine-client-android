package com.tuisongbao.engine.demo.chat;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.google.gson.JsonObject;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.chat.conversation.ChatConversation;
import com.tuisongbao.engine.chat.location.ChatLocationManager;
import com.tuisongbao.engine.chat.media.ChatVoiceRecorder;
import com.tuisongbao.engine.chat.message.ChatMessage;
import com.tuisongbao.engine.chat.message.ChatMessageContent;
import com.tuisongbao.engine.chat.message.ChatMessageImageContent;
import com.tuisongbao.engine.chat.message.ChatMessageLocationContent;
import com.tuisongbao.engine.chat.message.ChatMessageVoiceContent;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.GloableParams;
import com.tuisongbao.engine.demo.MainActivity_;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.bean.MessageStatus;
import com.tuisongbao.engine.demo.chat.adapter.MessageAdapter;
import com.tuisongbao.engine.demo.chat.callback.MessageCallback;
import com.tuisongbao.engine.demo.chat.widght.PasteEditText;
import com.tuisongbao.engine.demo.common.CommonUtils;
import com.tuisongbao.engine.demo.common.Utils;
import com.tuisongbao.engine.demo.view.activity.FriendMsgActivity_;
import com.tuisongbao.engine.demo.view.activity.GroupSettingActivity_;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FocusChange;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.Touch;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by user on 15-9-1.
 */
@EActivity(R.layout.activity_conversation)
public class ChatConversationActivity extends BaseActivity {
    public static final String EXTRA_CONVERSATION_TARGET = "EXTRA_CONVERSATION_TARGET";
    public static final String EXTRA_CONVERSATION_TYPE = "EXTRA_CONVERSATION_TYPE";
    static final int REQUEST_CODE_EMPTY_HISTORY = 2;
    public static final int REQUEST_CODE_CONTEXT_MENU = 3;
    static final int REQUEST_CODE_MAP = 4;
    public static final int REQUEST_CODE_TEXT = 5;
    public static final int REQUEST_CODE_VOICE = 6;
    public static final int REQUEST_CODE_PICTURE = 7;
    public static final int REQUEST_CODE_LOCATION = 8;
    public static final int REQUEST_CODE_NET_DISK = 9;
    public static final int REQUEST_CODE_FILE = 10;
    public static final int REQUEST_CODE_COPY_AND_PASTE = 11;
    public static final int REQUEST_CODE_PICK_VIDEO = 12;
    public static final int REQUEST_CODE_DOWNLOAD_VIDEO = 13;
    public static final int REQUEST_CODE_VIDEO = 14;
    public static final int REQUEST_CODE_DOWNLOAD_VOICE = 15;
    public static final int REQUEST_CODE_SELECT_USER_CARD = 16;
    public static final int REQUEST_CODE_SEND_USER_CARD = 17;
    public static final int REQUEST_CODE_CAMERA = 18;
    public static final int REQUEST_CODE_LOCAL = 19;
    public static final int REQUEST_CODE_CLICK_DESTORY_IMG = 20;
    public static final int REQUEST_CODE_GROUP_DETAIL = 21;
    public static final int REQUEST_CODE_SELECT_VIDEO = 23;
    public static final int REQUEST_CODE_SELECT_FILE = 24;
    public static final int REQUEST_CODE_ADD_TO_BLACKLIST = 25;

    public static final int RESULT_CODE_COPY = 1;
    public static final int RESULT_CODE_DELETE = 2;
    public static final int RESULT_CODE_FORWARD = 3;
    public static final int RESULT_CODE_OPEN = 4;
    public static final int RESULT_CODE_DWONLOAD = 5;
    public static final int RESULT_CODE_TO_CLOUD = 6;
    public static final int RESULT_CODE_EXIT_GROUP = 7;

    public static final int CHATTYPE_SINGLE = 1;
    public static final int CHATTYPE_GROUP = 2;
    public static final String COPY_IMAGE = "EASEMOBIMG";
    private List<ChatMessage> mMessageList;


    @ViewById(R.id.view_talk)
    View recordingContainer;

    @ViewById(R.id.mic_image)
    ImageView micImage;

    @ViewById(R.id.recording_hint)
    TextView recordingHint;

    @ViewById(R.id.list)
    ListView listView;

    @ViewById(R.id.et_sendmessage)
    PasteEditText mEditTextContent;

    @ViewById(R.id.btn_set_mode_keyboard)
    View buttonSetModeKeyboard;
    @ViewById(R.id.btn_set_mode_voice)
    View buttonSetModeVoice;

    @ViewById(R.id.btn_send)
    View buttonSend;

    @ViewById(R.id.btn_press_to_speak)
    View buttonPressToSpeak;

    @ViewById(R.id.btn_press_to_speak)
    LinearLayout ll_btn_container;
    @ViewById(R.id.more)
    View more;

    @ViewById(R.id.txt_title)
    TextView txt_title;

    @ViewById(R.id.img_right)
    ImageView img_right;

    @ViewById(R.id.img_back)
    ImageView img_back;

    @ViewById(R.id.mic_image)
    ImageView iv_emoticons_checked;

    @ViewById(R.id.edittext_layout)
    RelativeLayout edittext_layout;

    @ViewById(R.id.pb_load_more)
    ProgressBar loadmorePB;

    @ViewById(R.id.ll_btn_container)
    LinearLayout btnContainer;

    private File cameraFile;

    boolean isloading;
    final int pagesize = 20;
    boolean haveMoreData = true;
    @ViewById(R.id.btn_more)
    Button btnMore;
    public String playMsgId;
    private Long mStartMessageId = null;

    private Context mContext;

    InputMethodManager manager;

    AnimationDrawable animationDrawable;

    MessageAdapter messageAdapter;

    @Extra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET)
    String conversationTarget;

    @Extra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE)
    ChatType conversationType;

    ChatConversation mConversation;
    private ChatVoiceRecorder voiceRecorder;
    Emitter.Listener mListener;

    private EngineCallback<ChatMessage> sendTextMessageCallback = new EngineCallback<ChatMessage>() {

        @Override
        public void onSuccess(final ChatMessage t) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO send message success tip
                    Utils.showShortToast(mContext, "发送成功");
                }
            });
        }

        @Override
        public void onError(final ResponseError error) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // TODO send message failed handler
                    Utils.showShortToast(mContext, "发送失败");
                }
            });
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Utils.finish(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @AfterViews
    void afterViews() {
        if (mConversation == null) {
            return;
        }
        mContext = this;
        img_back.setVisibility(View.VISIBLE);
        img_right.setVisibility(View.VISIBLE);
        String name = conversationTarget;
        if (mConversation.getType().equals(ChatType.GroupChat)) {
            if (GloableParams.GroupInfos.containsKey(conversationTarget)) {
                name = GloableParams.GroupInfos.get(conversationTarget).getName();
            } else {
                name = "未命名群组";
            }
            img_right.setImageResource(R.drawable.icon_groupinfo);
        } else {
            img_right.setImageResource(R.drawable.icon_chat_user);
        }
        if (!TextUtils.isEmpty(name)) {
            txt_title.setText(name);
        }
        initView();
        mMessageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, conversationTarget, mMessageList);
        listView.setAdapter(messageAdapter);
        listView.setOnScrollListener(new ListScrollListener());
        loadMoreMessage();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @AfterExtras
    public void afterExtras() {
        mConversation = App.getInstance2().getConversationManager().loadOne(conversationTarget, conversationType);
        mConversation.resetUnread(new EngineCallback<String>() {
            @Override
            public void onSuccess(String s) {

            }

            @Override
            public void onError(ResponseError error) {

            }
        });
        Log.i("after extras", "----------------------" + mConversation.listeners(ChatConversation.EVENT_MESSAGE_NEW).size());

        mListener = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                final ChatMessage message = (ChatMessage)args[0];
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("got new message", message + "");
                        mMessageList.add(message);
                        messageAdapter.refresh(mMessageList);
                        scrollToBotton();
                    }
                });

            }
        };

        mConversation.bind(ChatConversation.EVENT_MESSAGE_NEW, mListener);
        Log.i("after extras2", "----------------------" + mConversation.listeners(ChatConversation.EVENT_MESSAGE_NEW).size());
        voiceRecorder = new ChatVoiceRecorder();
        voiceRecorder.setMinDuration(2 * 1000, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "duration is " + args[0] + ", less then min duration " + args[1], Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        voiceRecorder.setMaxDuration(10 * 1000, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                onRecordFinished();
                onRecordStart();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("destory", "-----------------" + mConversation.listeners(ChatConversation.EVENT_MESSAGE_NEW));
        mConversation.unbind(ChatManager.EVENT_MESSAGE_NEW);
        mConversation.resetUnread(new EngineCallback<String>() {
            @Override
            public void onSuccess(String s) {

            }

            @Override
            public void onError(ResponseError error) {

            }
        });
    }

    private ChatMessage generateSendingMsg(ChatMessageContent content){
        ChatMessage message= new ChatMessage(App.getInstance2().getEngine());
        Long messageId = 0L;
        if(!mMessageList.isEmpty()){
            messageId = mMessageList.get(mMessageList.size()-1).getMessageId() + 1;
        }
        message.setMessageId(messageId);
        message.setFrom(App.getInstance2().getChatUser().getUserId());
        message.setRecipient(mConversation.getTarget());
        message.setCreatedAt(new Date().toString());
        message.setContent(content);
        return message;
    }

    private void onRecordStart() {
        voiceRecorder.start();
    }

    private void onRecordFinished() {
        String filePath = voiceRecorder.stop();
        if (!TextUtils.isEmpty(filePath)) {
            sendVoiceMessage(filePath);
        }
    }

    private void sendVoiceMessage(String filePath) {
        ChatMessageVoiceContent content = new ChatMessageVoiceContent();
        content.setFilePath(filePath);
        Log.i("send file", filePath);
        ChatMessage message = generateSendingMsg(content);
        sendMediaMessage(message);
    }

    protected void initView() {
        animationDrawable = (AnimationDrawable) micImage.getBackground();
        animationDrawable.setOneShot(false);
        iv_emoticons_checked.setVisibility(View.INVISIBLE);
        edittext_layout.setBackgroundResource(R.drawable.input_bar_bg_normal);
        edittext_layout.requestFocus();
    }

    /**
     * 隐藏软键盘
     */
    private void hideKeyboard() {
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                manager.hideSoftInputFromWindow(getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Click(R.id.img_right)
    void clickRight() {
        hideKeyboard();
        Intent intent;
        if (mConversation.getType().equals(ChatType.SingleChat)) {
            intent = new Intent(this, FriendMsgActivity_.class);

        } else {
            intent = new Intent(this, GroupSettingActivity_.class);
        }
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET, mConversation.getTarget());
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE, mConversation.getType());
        startActivity(intent);
    }

    @Click(R.id.btn_send)
    void sendText(){
//        hideKeyboard();
        String text = mEditTextContent.getText().toString();
        ChatMessageContent content = new ChatMessageContent();
        content.setType(ChatMessage.TYPE.TEXT);
        content.setText(text);
        ChatMessage message = generateSendingMsg(content);
        mMessageList.add(message);
        messageAdapter.refresh(mMessageList);
        scrollToBotton();
        mConversation.sendMessage(content, sendTextMessageCallback, null);
        mEditTextContent.setText("");
        setResult(RESULT_OK);
    }

    void scrollToBotton(){
        int selectionPosition = mMessageList.size();
        listView.setSelection(selectionPosition);
    }

    /**
     * 发送图片
     *
     * @param filePath
     */
    private void sendImage(final String filePath) {
        ChatMessageImageContent content = new ChatMessageImageContent();
        Log.i("sending file path:", filePath);
        content.setFilePath(filePath);
        ChatMessage message = generateSendingMsg(content);
        sendMediaMessage(message);
    }

    void sendMediaMessage(final ChatMessage message){
        JsonObject json = new JsonObject();
        json.addProperty("status", MessageStatus.INPROGRESS.getValue());
        message.getContent().setExtra(json);
        mMessageList.add(message);
        messageAdapter.refresh(mMessageList);
        scrollToBotton();
        mConversation.sendMessage(message.getContent(), new MessageCallback(message), new ProgressCallback() {

            @Override
            public void progress(int percent) {
                JsonObject json;
                if (message.getContent().getExtra() == null) {
                    json = new JsonObject();
                } else {
                    json = message.getContent().getExtra();
                }

                json.addProperty("progress", percent);
                message.getContent().setExtra(json);
            }
        });
        setResult(RESULT_OK);
    }

    /**
     * 根据图库图片uri发送图片
     *
     * @param selectedImage
     */
    private void sendPicByUri(Uri selectedImage) {
        // String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(selectedImage, null, null,
                null, null);
        String st8 = getResources().getString(R.string.cant_find_pictures);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex("_data");
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            if (picturePath == null || picturePath.equals("null")) {
                Toast toast = Toast.makeText(this, st8, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;
            }
            sendImage(picturePath);
        } else {
            File file = new File(selectedImage.getPath());
            if (!file.exists()) {
                Toast toast = Toast.makeText(this, st8, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                return;

            }
            sendImage(file.getAbsolutePath());
        }

    }

    @OnActivityResult(REQUEST_CODE_LOCAL)
    void onResultLocalImage(int resultCode, Intent data) {
        if (data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                sendPicByUri(selectedImage);
            }
        }
    }

    @OnActivityResult(REQUEST_CODE_CAMERA)
    void onResultCAMERA(int resultCode, Intent data) {
        if (cameraFile != null && cameraFile.exists())
            sendImage(cameraFile.getAbsolutePath());
    }



    /**
     * 显示或隐藏图标按钮页
     *
     * @param view
     */
    public void more(View view) {
        if (more.getVisibility() == View.GONE) {
            System.out.println("more gone");
            hideKeyboard();
            more.setVisibility(View.VISIBLE);
            btnContainer.setVisibility(View.VISIBLE);
        } else {
            more.setVisibility(View.GONE);
        }

    }

    /**
     * 照相获取图片
     */
    @Click(R.id.view_camera)
    public void selectPicFromCamera() {
        hideKeyboard();
        if (!CommonUtils.isExitsSdcard()) {
            String st = getResources().getString(
                    R.string.sd_card_does_not_exist);
            Utils.showShortToast(this, st);
            return;
        }
        cameraFile = new File(App.getDemoDownLoadDir() + "/" + System.currentTimeMillis() + ".jpg");
        cameraFile.getParentFile().mkdirs();
        startActivityForResult(
                new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
                        MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile)),
                REQUEST_CODE_CAMERA);
    }

    /**
     * 从图库获取图片
     */
    @Click(R.id.view_photo)
    public void selectPicFromLocal() {
        hideKeyboard();
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

        } else {
            intent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, REQUEST_CODE_LOCAL);

    }

    @Click(R.id.view_location)
    public void sendLocation() {
        ChatLocationManager.getInstance().getCurrentLocation(new EngineCallback<Location>() {
            @Override
            public void onSuccess(Location location) {
                ChatMessageLocationContent content = new ChatMessageLocationContent(location);
                ChatMessage message = generateSendingMsg(content);
                sendMediaMessage(message);
            }

            @Override
            public void onError(ResponseError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showShortToast(mContext, "send message error");
                    }
                });
            }
        }, 5);
    }

    @Click({ R.id.view_file,  R.id.view_video, R.id.view_audio})
    public void sendMore(){
        Utils.showShortToast(this, "尽情期待");
    }

    /**
     * 显示语音图标按钮
     *
     * @param view
     */
    public void setModeVoice(View view) {
        hideKeyboard();
        edittext_layout.setVisibility(View.GONE);
        more.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        buttonSetModeKeyboard.setVisibility(View.VISIBLE);
        buttonSend.setVisibility(View.GONE);
        btnMore.setVisibility(View.VISIBLE);
        buttonPressToSpeak.setVisibility(View.VISIBLE);
        iv_emoticons_checked.setVisibility(View.INVISIBLE);
        btnContainer.setVisibility(View.VISIBLE);
    }

    /**
     * 显示键盘图标
     *
     * @param view
     */
    public void setModeKeyboard(View view) {
        edittext_layout.setVisibility(View.VISIBLE);
        more.setVisibility(View.GONE);
        view.setVisibility(View.GONE);
        buttonSetModeVoice.setVisibility(View.VISIBLE);
        // mEditTextContent.setVisibility(View.VISIBLE);
        mEditTextContent.requestFocus();
        // buttonSend.setVisibility(View.VISIBLE);
        buttonPressToSpeak.setVisibility(View.GONE);
        if (TextUtils.isEmpty(mEditTextContent.getText())) {
            btnMore.setVisibility(View.VISIBLE);
            buttonSend.setVisibility(View.GONE);
        } else {
            btnMore.setVisibility(View.GONE);
            buttonSend.setVisibility(View.VISIBLE);
        }

    }

    @FocusChange(R.id.et_sendmessage)
    void editTextFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            edittext_layout
                    .setBackgroundResource(R.drawable.input_bar_bg_active);
        } else {
            edittext_layout
                    .setBackgroundResource(R.drawable.input_bar_bg_normal);
        }
    }

    @Click(R.id.et_sendmessage)
    void editTextClick(View v) {
        edittext_layout
                .setBackgroundResource(R.drawable.input_bar_bg_active);
        more.setVisibility(View.GONE);
        iv_emoticons_checked.setVisibility(View.INVISIBLE);
        btnContainer.setVisibility(View.GONE);
    }

    @TextChange(R.id.et_sendmessage)
    void onTextChanged(CharSequence s, int start, int before,
                       int count) {
        if (!TextUtils.isEmpty(s)) {
            btnMore.setVisibility(View.GONE);
            buttonSend.setVisibility(View.VISIBLE);
        } else {
            btnMore.setVisibility(View.VISIBLE);
            buttonSend.setVisibility(View.GONE);
        }
    }

    @Click(R.id.img_back)
    void back() {
        Utils.start_Activity(this, MainActivity_.class);
        finish();
    }

    /**
     * 覆盖手机返回键
     */
    @Override
    public void onBackPressed() {
        if (more.getVisibility() == View.VISIBLE) {
            more.setVisibility(View.GONE);
            iv_emoticons_checked.setVisibility(View.INVISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Touch(R.id.btn_press_to_speak)
    boolean pressToSpeak(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                animationDrawable.start();
                if (!CommonUtils.isExitsSdcard()) {
                    String st4 = getResources().getString(
                            R.string.Send_voice_need_sdcard_support);
                    Toast.makeText(ChatConversationActivity.this, st4, Toast.LENGTH_SHORT)
                            .show();
                    return false;
                }
                try {
                    v.setPressed(true);
                    recordingContainer.setVisibility(View.VISIBLE);
                    recordingHint
                            .setText(getString(R.string.move_up_to_cancel));
                    recordingHint.setBackgroundColor(Color.TRANSPARENT);
                    onRecordStart();
                } catch (Exception e) {
                    e.printStackTrace();
                    v.setPressed(false);
                    if (voiceRecorder != null)
                        voiceRecorder.cancel();
                    recordingContainer.setVisibility(View.INVISIBLE);
                    Toast.makeText(ChatConversationActivity.this, R.string.recoding_fail,
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

                return true;
            case MotionEvent.ACTION_MOVE: {
                if (event.getY() < 0) {
                    recordingHint
                            .setText(getString(R.string.release_to_cancel));
                    recordingHint
                            .setBackgroundResource(R.drawable.recording_text_hint_bg);
                } else {
                    recordingHint
                            .setText(getString(R.string.move_up_to_cancel));
                    recordingHint.setBackgroundColor(Color.TRANSPARENT);
                    animationDrawable.start();
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
                if (animationDrawable.isRunning()) {
                    animationDrawable.stop();
                }

                v.setPressed(false);
                recordingContainer.setVisibility(View.INVISIBLE);
                if (event.getY() < 0) {
                    // discard the recorded audio.
                    voiceRecorder.cancel();

                } else {
                    onRecordFinished();

                }
                return true;
            default:
                recordingContainer.setVisibility(View.INVISIBLE);
                if (voiceRecorder != null)
                    voiceRecorder.cancel();
                return false;
        }
    }

    void loadMoreMessage(){
        mConversation.getMessages(mStartMessageId, null, pagesize,
                new EngineCallback<List<ChatMessage>>() {

                    @Override
                    public void onSuccess(final List<ChatMessage> t) {
                        Log.i("got message size", t.size() + "");
                        if (t.size() != pagesize){
                            haveMoreData = false;
                        }

                        if (t.size() < 1) {
                            haveMoreData = false;
                            return;
                        }
                        final int selectionPosition = t.size();
                        mStartMessageId = t.get(0).getMessageId();
                        mStartMessageId = mStartMessageId - Long.valueOf(t.size());
                        Collections.reverse(t);
                        t.addAll(mMessageList);
                        mMessageList = t;


                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                messageAdapter.refresh(mMessageList);
                                listView.setSelection(selectionPosition);
                                loadmorePB.setVisibility(View.GONE);
                                isloading = false;
                            }
                        });
                    }

                    @Override
                    public void onError(ResponseError error) {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                loadmorePB.setVisibility(View.GONE);
                                Toast.makeText(ChatConversationActivity.this,
                                        "获取消息失败，请稍后再试", Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }
                });
    }

    /**
     * listview滑动监听listener
     *
     */
    private class ListScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    if (view.getFirstVisiblePosition() == 0 && !isloading
                            && haveMoreData) {
                        loadmorePB.setVisibility(View.VISIBLE);
                        // sdk初始化加载的聊天记录为20条，到顶时去db里获取更多
                        try {
                            loadMoreMessage();
                        } catch (Exception e1) {
                            loadmorePB.setVisibility(View.GONE);
                            return;
                        }
                    }
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

        }

    }
}

