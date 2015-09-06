package com.tuisongbao.engine.demo.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

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

    @ViewById(R.id.txt_title)
    TextView txt_title;

    @ViewById(R.id.img_back)
    ImageView img_back;

    private ChatConversation mConversation;

    @AfterExtras
    public void afterExtras() {
        mConversation = App.getInstance2().getConversationManager().loadOne(conversationTarget, conversationType);
    }

    @Click(R.id.img_back)
    void back() {
        Utils.finish(FriendMsgActivity.this);
    }

    @AfterViews
    public void afterViews() {
        if (mConversation == null) {
            return;
        }
        img_back.setVisibility(View.VISIBLE);
        txt_title.setText(mConversation.getTarget());
        final List<String> names = new ArrayList<>();

        names.add(App.getInstance2().getChatUser().getUserId());
        names.add(conversationTarget);

        GroupUserAdapter adapter = new GroupUserAdapter(names, this);
        userList.setAdapter(adapter);
    }


    @Click(R.id.delete_conversation)
    public void deleteConversation() {
        final Activity acitvity = this;
        mConversation.delete(new EngineCallback<String>() {
            @Override
            public void onSuccess(String s) {
                Intent intent = new Intent(FriendMsgActivity.this, MainActivity_.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(ResponseError error) {
                Utils.showLongToast(acitvity, "删除失败");
            }
        });
    }
}
