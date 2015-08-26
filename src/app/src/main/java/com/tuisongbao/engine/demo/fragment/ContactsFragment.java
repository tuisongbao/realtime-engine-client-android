package com.tuisongbao.engine.demo.fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.activity.ChatConversationActivity;
import com.tuisongbao.engine.demo.activity.ChatConversationActivity_;
import com.tuisongbao.engine.demo.activity.GroupsActivity_;
import com.tuisongbao.engine.demo.app.App;
import com.tuisongbao.engine.demo.utils.AppToast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by user on 15-8-14.
 */
@EFragment(R.layout.fragment_contacts)
public class ContactsFragment extends Fragment {
    @ViewById(R.id.cgt_tv_userName)
    TextView userName;

    @ViewById(R.id.cgt_iv_userPhoto)
    ImageView userAvatar;

    String username;

    @AfterViews
    void afterViews(){
        username = App.getContext().getUser().getUserId();
        userName.setText(username);
        ImageLoader.getInstance().displayImage(Constants.USERAVATARURL + username, userAvatar);

    }

    @Click(R.id.qun_zhu)
    void gotoGroupListActivity(){
        AppToast.getToast().show("群组列表");
        Intent intent = new Intent(this.getActivity(),
                GroupsActivity_.class);
        startActivity(intent);
    }

    @Click(R.id.call_self)
    void gotoConversation(){
        AppToast.getToast().show("和自己说话");
        Intent intent = new Intent(this.getActivity(),
                ChatConversationActivity_.class);
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET, username);
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE, ChatType.SingleChat);

        startActivity(intent);
    }

}
