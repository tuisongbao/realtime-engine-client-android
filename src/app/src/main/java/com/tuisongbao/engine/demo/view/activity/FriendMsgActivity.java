package com.tuisongbao.engine.demo.view.activity;

import android.content.Intent;
import android.view.MenuItem;
import android.widget.GridView;

import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.chat.conversation.ChatConversation;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.MainActivity_;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.chat.ChatConversationActivity;
import com.tuisongbao.engine.demo.chat.adapter.GroupUserAdapter;
import com.tuisongbao.engine.demo.common.Utils;
import com.tuisongbao.engine.demo.view.BaseActivity;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 15-9-2.
 */
@EActivity(R.layout.activity_friendmsg)
public class FriendMsgActivity extends BaseActivity {
    @Extra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET)
    String conversationTarget;

    @Extra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE)
    ChatType conversationType;

    @ViewById(R.id.user_list)
    GridView userList;

    private ChatConversation mConversation;

    @AfterExtras
    public void doSomethingAfterExtrasInjection() {
        mConversation = App.getInstance2().getConversationManager().loadOne(conversationTarget, conversationType);
    }

    @AfterViews
    public void afterViews() {
        if (mConversation == null) {
            return;
        }

        final List<String> names = new ArrayList<>();

        names.add(App.getInstance2().getChatUser().getUserId());
        names.add(conversationTarget);

        GroupUserAdapter adapter = new GroupUserAdapter(names, this);
        userList.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home://返回上一菜单页
                Utils.finish(FriendMsgActivity.this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Click(R.id.delete_conversation)
    public void deleteConversation() {
        mConversation.delete(new EngineCallback<String>() {
            @Override
            public void onSuccess(String s) {
                Intent intent = new Intent(FriendMsgActivity.this, MainActivity_.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(ResponseError error) {

            }
        });
    }
}
