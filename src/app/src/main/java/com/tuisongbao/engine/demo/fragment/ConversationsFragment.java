package com.tuisongbao.engine.demo.fragment;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.adapter.ConversationAdapter;
import com.tuisongbao.engine.demo.app.App;
import com.tuisongbao.engine.demo.utils.LogUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
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
}
