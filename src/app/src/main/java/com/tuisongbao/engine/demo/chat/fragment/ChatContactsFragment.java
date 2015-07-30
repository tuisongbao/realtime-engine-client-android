package com.tuisongbao.engine.demo.chat.fragment;

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

import com.tuisongbao.engine.chat.entity.ChatType;
import com.tuisongbao.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.engine.chat.entity.TSBContactsUser;
import com.tuisongbao.engine.demo.DemoApplication;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.chat.ChatConversationActivity;
import com.tuisongbao.engine.demo.chat.ChatGroupsActivity;
import com.tuisongbao.engine.demo.chat.adapter.ChatListFriendAdapter;
import com.tuisongbao.engine.demo.chat.cache.LoginCache;

public class ChatContactsFragment extends Fragment {
    private static ChatContactsFragment mChatContactsFragment;

    private View mRootView;
    private LinearLayout mGroupLayout;
    private ListView mFriendsListView;
    private ChatListFriendAdapter mFriendsAdapter;
    private List<TSBContactsUser> mFriendsList;

    public static ChatContactsFragment getInstance() {
        if (null == mChatContactsFragment) {
            mChatContactsFragment = new ChatContactsFragment();
        }
        return mChatContactsFragment;
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
        mRootView = inflater.inflate(R.layout.fragment_contacts, container,
                false);
        mFriendsListView = (ListView) mRootView
                .findViewById(R.id.fragment_contacts_friends_listview);
        mGroupLayout = (LinearLayout) mRootView
                .findViewById(R.id.fragment_contacts_group);
        mGroupLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),
                        ChatGroupsActivity.class);
                startActivity(intent);
            }
        });

        mFriendsList = new ArrayList<TSBContactsUser>(LoginCache.getAddedUserList());
        mFriendsAdapter = new ChatListFriendAdapter(mFriendsList, getActivity());
        mFriendsListView.setAdapter(mFriendsAdapter);
        mFriendsListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                Intent intent = new Intent(getActivity(),
                        ChatConversationActivity.class);

                TSBChatConversation conversation = new TSBChatConversation(DemoApplication.engine.chatManager.conversationManager);
                conversation.setTarget(mFriendsList.get(arg2).getUserId());
                conversation.setType(ChatType.SingleChat);

                intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION, conversation);
                startActivity(intent);
            }
        });

        return mRootView;
    }

    public void refresh() {
        mFriendsList = new ArrayList<TSBContactsUser>(LoginCache.getAddedUserList());
        mFriendsAdapter.refresh(mFriendsList);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
