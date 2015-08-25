package com.tuisongbao.engine.demo.fragment;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.activity.ChatConversationActivity;
import com.tuisongbao.engine.demo.activity.ChatConversationActivity_;
import com.tuisongbao.engine.demo.adapter.ConversationAdapter;
import com.tuisongbao.engine.demo.app.App;
import com.tuisongbao.engine.demo.utils.L;
import com.tuisongbao.engine.demo.utils.LogUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 15-8-14.
 */
@EFragment(R.layout.fragment_conversations)
public class ConversationsFragment extends Fragment {
    private static final String TAG = LogUtil.makeLogTag(ConversationsFragment.class);

    @ViewById(R.id.fg_conversations_listView)
    ListView listView;

    List<ChatConversation> mConversationList;

    @Bean
    ConversationAdapter mConversationAdapter;

    @AfterViews
    void calledAfterViewInjection() {
        if(mConversationList == null){
            mConversationList = new ArrayList<>();
        }

        mConversationAdapter.setmList(mConversationList);

        if (listView.getAdapter() == null) {
            listView.setAdapter(mConversationAdapter);
        } else {
            mConversationAdapter.notifyDataSetChanged();
        }

        request();
    }

    void request() {
        App.getContext().getConversationManager().getList(null, null, new EngineCallback<List<ChatConversation>>() {

            public void onSuccess(final List<ChatConversation> t) {
                Log.d(TAG, "Get " + t.size() + " conversations");
                Activity activity = getActivity();
                if (activity == null) {
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConversationList = t;
                        mConversationAdapter.refresh(mConversationList);

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

    @ItemClick(R.id.fg_conversations_listView)
    public void myListItemClicked(int position) {
        L.i(TAG, "--------------click" + position);
        ChatConversation mClickedChatConversation = mConversationList.get(position);
        resetUnread(mClickedChatConversation);

        mConversationAdapter.refresh(mConversationList);

        Intent intent = new Intent(this.getActivity(),
                ChatConversationActivity_.class);
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET, mClickedChatConversation.getTarget());
        intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE, mClickedChatConversation.getType());

        startActivity(intent);
    }

    @Background
    void resetUnread(ChatConversation conversation) {
        conversation.setUnreadMessageCount(0);
        conversation.resetUnread(new EngineCallback<String>() {
            @Override
            public void onSuccess(String t) {
                Toast.makeText(getActivity(), "重置未读消息成功", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(ResponseError error) {
                Toast.makeText(getActivity(), "重置未读消息失败，请稍后再试", Toast.LENGTH_LONG).show();
            }
        });
    }
}
