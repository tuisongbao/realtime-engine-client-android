package com.tuisongbao.android.engine.demo.chat.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.ChatGroupDetailActivity;
import com.tuisongbao.android.engine.demo.chat.adapter.ChatTalkAdapter;

public class ChatTalkFragment extends Fragment {

    private static ChatTalkFragment mChatTalkFragment;

    private View mRootView;
    private ListView mListViewTalk;
    private List<TSBChatConversation> mListConversation;
    private ChatTalkAdapter mAdapterChatTalk;

    public static ChatTalkFragment getInstance() {
        if (null == mChatTalkFragment) {
            mChatTalkFragment = new ChatTalkFragment();
        }
        return mChatTalkFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_chat_talk, container,
                false);
        mListViewTalk = (ListView) mRootView
                .findViewById(R.id.fragment_chat_talk_listview);

        mListConversation = new ArrayList<TSBChatConversation>();
        TSBChatConversation conversation = new TSBChatConversation();
        conversation.setLastActiveAt("2014-11-22");
        conversation.setTarget("aaaaaaaa");
        conversation.setUnreadMessageCount(111);
        mListConversation.add(conversation);

        conversation = new TSBChatConversation();
        conversation.setLastActiveAt("2014-11-23");
        conversation.setTarget("bbbb");
        conversation.setUnreadMessageCount(41);
        mListConversation.add(conversation);

        conversation = new TSBChatConversation();
        conversation.setLastActiveAt("2014-11-24");
        conversation.setTarget("cc");
        conversation.setUnreadMessageCount(23);
        mListConversation.add(conversation);

        mAdapterChatTalk = new ChatTalkAdapter(mListConversation, getActivity());
        mListViewTalk.setAdapter(mAdapterChatTalk);
        mListViewTalk.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                Intent intent = new Intent(getActivity(),
                        ChatGroupDetailActivity.class);
                startActivity(intent);
            }
        });

        return mRootView;
    }
}
