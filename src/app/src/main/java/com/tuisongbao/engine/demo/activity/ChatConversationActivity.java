package com.tuisongbao.engine.demo.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.adapter.ChatMessagesAdapter;
import com.tuisongbao.engine.demo.app.App;
import com.tuisongbao.engine.demo.entity.DemoGroup;
import com.tuisongbao.engine.demo.utils.AppToast;
import com.tuisongbao.engine.demo.utils.DemoGroupUtil;
import com.tuisongbao.engine.demo.utils.LogUtil;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by user on 15-8-24.
 */
@EActivity(R.layout.activity_conversation)
public class ChatConversationActivity extends BaseActivity{
    public static final String EXTRA_CONVERSATION_TARGET = "EXTRA_CONVERSATION_TARGET";
    public static final String EXTRA_CONVERSATION_TYPE = "EXTRA_CONVERSATION_TYPE";
    private static final String TAG = LogUtil.makeLogTag(ChatConversationActivity.class);
    private ChatConversation mConversation;

    @Bean
    DemoGroupUtil demoGroupUtil;

    @ViewById(R.id.cgt_lv_chat_showBox)
    ListView mMessagesListView;

    @ViewById(R.id.cgt_pb_chat_loading)
    ProgressBar mPb_chat_loading;

    @Extra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET)
    String conversationTarget;

    @Extra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE)
    ChatType conversationType;

    private List<ChatMessage> mMessageList;

    ChatMessagesAdapter mMessagesAdapter;

    /**
     * 查询到的数据条数
     */
    private int findChatMsgCount = 0;

    /**
     * 是否正在加载数据中
     */
    private boolean isLoading;

    private Long mStartMessageId = null;

    @AfterExtras
    public void doSomethingAfterExtrasInjection() {
        mConversation = App.getContext().getConversationManager().loadOne(conversationTarget,conversationType);
    }

    @AfterViews
    public void afterViews() {
        initView();
    }

    private void initView() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        final String name = mConversation.getTarget();
        actionBar.setTitle(name);

        if (ChatType.GroupChat.equals(mConversation.getType())){
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    DemoGroup demoGroup = demoGroupUtil.getDemoGroup(name);
                    if(demoGroup != null){
                        actionBar.setTitle(demoGroup.getName());
                    }
                }
            });
        }

        setOverflowShowingAlways();
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setData();
        request();
    }

    /**
     * 刷新聊天数据
     */
    private void setData() {
        mPb_chat_loading.setVisibility(View.GONE);

        if (mMessagesAdapter == null) {
            if(mMessageList == null){
                mMessageList = new ArrayList<>();
            }
            mMessagesAdapter = new ChatMessagesAdapter(ChatConversationActivity.this, mMessageList);
            mMessagesListView.setAdapter(mMessagesAdapter);
        } else {
            mMessagesAdapter.notifyDataSetChanged();
        }

        isLoading = false;
        if (findChatMsgCount > 0) {
            mMessagesListView.setSelection(findChatMsgCount - 1);
        }
    }

    private void request() {
        mConversation.getMessages(mStartMessageId, null, 20,
            new EngineCallback<List<ChatMessage>>() {

                @Override
                public void onSuccess(final List<ChatMessage> t) {
                    Log.d(TAG, "Get " + t.size() + " messages");
                    if (t.size() < 1) {
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
                            mMessagesAdapter.refresh(mMessageList);
                            mMessagesListView
                                    .setSelection(selectionPosition);
                            mPb_chat_loading.setVisibility(View.VISIBLE);
                        }
                    });
                }

                @Override
                public void onError(ResponseError error) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.demo_menu_chat, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home://返回上一菜单页
                AppToast.getToast().show("返回上一页");
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                } else {
                    upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    NavUtils.navigateUpTo(this, upIntent);
                }
                break;
            case R.id.menu_chat_chatInfo:
                AppToast.getToast().show(R.string.text_menu_chatInfo);
                Intent intent;
                if (mConversation.getType().equals(ChatType.SingleChat)){
                    intent = new Intent(this, ConversationInfoActivity_.class);

                }else {
                    AppToast.getToast().show("组信息");
                    intent = new Intent(this, GroupInfoActivity_.class);
                }
                intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET, mConversation.getTarget());
                intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE, mConversation.getType());
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
