package com.tuisongbao.engine.demo.view.fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.widget.ImageView;
import android.widget.TextView;

import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.chat.ChatUser;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.chat.ChatConversationActivity;
import com.tuisongbao.engine.demo.chat.ChatConversationActivity_;
import com.tuisongbao.engine.demo.net.NetClient;
import com.tuisongbao.engine.demo.view.activity.GroupListActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by user on 15-9-1.
 */
@EFragment(R.layout.fragment_contacts)
public class ContactsFragment extends Fragment {
    @ViewById(R.id.tvname)
    TextView userName;

    @ViewById(R.id.head)
    ImageView userAvatar;

    String username;

    @AfterViews
    void afterViews(){
        ChatUser chatUser = App.getInstance().getChatUser();
        if(chatUser != null){
            username = chatUser.getUserId();
        }else {
            username = "";
        }
        userName.setText(username);
    }

    public void updateAvatar(){
        NetClient.showAvatar(userAvatar, username);
    }

    @Click(R.id.qun_zhu)
    void gotoGroupListActivity(){
        Intent intent = new Intent(this.getActivity(),
                GroupListActivity_.class);
        startActivity(intent);
    }

    @Click(R.id.call_self)
    void gotoConversation(){
        Intent intent = new Intent(this.getActivity(),
                ChatConversationActivity_.class);
        username = App.getInstance().getChatUser().getUserId();
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET, username);
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE, ChatType.SingleChat);
        startActivity(intent);
    }
}
