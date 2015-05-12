package com.tuisongbao.android.engine.demo.chat.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tuisongbao.android.engine.chat.entity.ChatType;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupUser;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.ChatGroupActivity;
import com.tuisongbao.android.engine.demo.chat.ChatConversationActivity;
import com.tuisongbao.android.engine.demo.chat.adapter.ChatListFriendAdapter;
import com.tuisongbao.android.engine.demo.chat.cache.LoginChache;

public class ChatListFragment extends Fragment {
    private static ChatListFragment mChatListFragment;

    private View mRootView;
    private LinearLayout mLayoutGroup;
    private ListView mListViewFriend;
    private ChatListFriendAdapter mAdapterFriend;
    private List<TSBChatGroupUser> mListUser;

    public static ChatListFragment getInstance() {
        if (null == mChatListFragment) {
            mChatListFragment = new ChatListFragment();
        }
        return mChatListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_chat_list, container,
                false);
        mListViewFriend = (ListView) mRootView
                .findViewById(R.id.fragment_chat_list_listview);
        mLayoutGroup = (LinearLayout) mRootView
                .findViewById(R.id.fragment_chat_list_group);
        mLayoutGroup.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),
                        ChatGroupActivity.class);
                startActivity(intent);
            }
        });

        mListUser = new ArrayList<TSBChatGroupUser>(LoginChache.getAddedUserList());
        mAdapterFriend = new ChatListFriendAdapter(mListUser, getActivity());
        mListViewFriend.setAdapter(mAdapterFriend);
        mListViewFriend.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                Intent intent = new Intent(getActivity(),
                        ChatConversationActivity.class);
                intent.putExtra(ChatConversationActivity.EXTRA_CODE_TARGET, mListUser.get(arg2).getUserId());
                intent.putExtra(ChatConversationActivity.EXTRA_CODE_CHAT_TYPE, ChatType.SingleChat.getName());
                startActivity(intent);
            }
        });
        

        return mRootView;
    }
    
    public void refresh() {
        mListUser = new ArrayList<TSBChatGroupUser>(LoginChache.getAddedUserList());
        mAdapterFriend.refresh(mListUser);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}
