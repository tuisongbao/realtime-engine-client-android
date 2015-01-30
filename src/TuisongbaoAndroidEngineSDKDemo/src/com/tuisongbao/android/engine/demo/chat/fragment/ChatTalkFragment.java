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
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
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
        request();

        return mRootView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        request();
    }
    
    private void request() {
        TSBChatManager.getInstance().getConversation(null, null, new TSBEngineCallback<List<TSBChatConversation>>() {
            
            @Override
            public void onSuccess(final List<TSBChatConversation> t) {
                mListConversation = t;
                getActivity().runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        mAdapterChatTalk.refresh(mListConversation);
                    }
                });
                
            }
            
            @Override
            public void onError(int code, String message) {
                getActivity().runOnUiThread(new Runnable() {
                    
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "获取会话失败，请稍后再试", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
