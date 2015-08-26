package com.tuisongbao.engine.demo.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import android.widget.GridView;

import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.chat.conversation.ChatConversation;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.MainActivity_;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.adapter.GroupUserAdapter;
import com.tuisongbao.engine.demo.app.App;
import com.tuisongbao.engine.demo.utils.AppToast;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 15-8-25.
 */
@EActivity(R.layout.activity_conversation_info)
public class ConversationInfoActivity extends BaseActivity{
    @Extra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET)
    String conversationTarget;

    @Extra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE)
    ChatType conversationType;

    @ViewById(R.id.user_list)
    GridView userList;

    private ChatConversation mConversation;

    @AfterExtras
    public void doSomethingAfterExtrasInjection() {
        mConversation = App.getContext().getConversationManager().loadOne(conversationTarget,conversationType);
    }

    @AfterViews
    public void afterViews() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        setOverflowShowingAlways();
        if (mConversation == null){
            return;
        }
        actionBar.setTitle(mConversation.getTarget());

        final List<String> names = new ArrayList<>();

        names.add(App.getContext().getUser().getUserId());
        names.add(conversationTarget);

        GroupUserAdapter adapter = new GroupUserAdapter(names, this);
        userList.setAdapter(adapter);
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
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Click(R.id.delete_conversation)
    public void deleteConversation(){
        mConversation.delete(new EngineCallback<String>() {
            @Override
            public void onSuccess(String s) {
                Intent intent = new Intent(ConversationInfoActivity.this, MainActivity_.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(ResponseError error) {

            }
        });
    }
}
