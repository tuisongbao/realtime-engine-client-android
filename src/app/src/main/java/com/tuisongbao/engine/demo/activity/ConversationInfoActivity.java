package com.tuisongbao.engine.demo.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.MainActivity_;
import com.tuisongbao.engine.demo.R;
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

        userList.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return names.size();
            }

            @Override
            public Object getItem(int position) {
                return names.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                convertView = View.inflate(ConversationInfoActivity.this, R.layout.demo_user_item, null);
                ImageView avatar = (ImageView)convertView.findViewById(R.id.avatar);
                TextView username = (TextView)convertView.findViewById(R.id.userName);
                String name = names.get(position);
                username.setText(name);
                ImageLoader.getInstance().displayImage(Constants.USERAVATARURL + name, avatar);

                return convertView;
            }
        });
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
