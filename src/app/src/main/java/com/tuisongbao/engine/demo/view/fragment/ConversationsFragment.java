package com.tuisongbao.engine.demo.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.conversation.ChatConversation;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.MainActivity;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.adpter.ConversationAdpter;
import com.tuisongbao.engine.demo.chat.ChatConversationActivity;
import com.tuisongbao.engine.demo.chat.ChatConversationActivity_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 15-9-1.
 */
@EFragment(R.layout.fragment_conversations)
public class ConversationsFragment extends Fragment {
    @ViewById(R.id.rl_error_item)
    public RelativeLayout errorItem;

    @ViewById(R.id.tv_connect_errormsg)
    public TextView errorText;

    @ViewById(R.id.listview)
    ListView lvContact;

    private Activity ctx;

    Emitter.Listener mListener;


    private ConversationAdpter adpter;
    private List<ChatConversation> conversationList = new ArrayList<ChatConversation>();
    private MainActivity parentActivity;

    @AfterViews
    void afterView(){
        ctx = this.getActivity();
        parentActivity = (MainActivity) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        conversationList.clear();
        initViews();
        mListener = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                });
            }
        };
        App.getInstance2().getChatManager().bind(ChatManager.EVENT_MESSAGE_NEW, mListener);
    }

    public void onPause() {
        super.onPause();
        App.getInstance2().getChatManager().unbind(ChatManager.EVENT_MESSAGE_NEW, mListener);
    }

    private void initViews() {
        if(conversationList == null){
            conversationList = new ArrayList<>();
        }

        if(adpter == null){
            adpter = new ConversationAdpter(ctx,conversationList);
        }

        adpter.setConversationList(conversationList);

        if (lvContact.getAdapter() == null) {
            lvContact.setAdapter(adpter);
        } else {
            adpter.notifyDataSetChanged();
        }
        refresh();
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        conversationList.clear();
        App.getInstance2().getConversationManager().getList(null, null, new EngineCallback<List<ChatConversation>>() {

            public void onSuccess(final List<ChatConversation> t) {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        conversationList = t;
                        adpter.refresh(conversationList);

                    }
                });

            }

            @Override
            public void onError(ResponseError error) {
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "获取会话失败，请稍后再试", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @ItemClick(R.id.listview)
    public void gotoConversation(int position) {
        ChatConversation mClickedChatConversation = conversationList.get(position);
        mClickedChatConversation.resetUnread(null);
        Intent intent = new Intent(this.getActivity(),
                ChatConversationActivity_.class);
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET, mClickedChatConversation.getTarget());
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE, mClickedChatConversation.getType());

        startActivity(intent);
    }

}
