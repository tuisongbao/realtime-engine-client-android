package com.tuisongbao.android.engine.demo.chat.fragment;

import java.util.ArrayList;
import java.util.List;

import com.tuisongbao.android.engine.chat.entity.TSBChatGroupUser;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.ChatGroupActivity;
import com.tuisongbao.android.engine.demo.chat.adapter.ChatListFriendAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

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

        mListUser = new ArrayList<TSBChatGroupUser>();
        TSBChatGroupUser user = new TSBChatGroupUser();
        user.setUserId("好友A");
        mListUser.add(user);
        mAdapterFriend = new ChatListFriendAdapter(mListUser, getActivity());
        mListViewFriend.setAdapter(mAdapterFriend);

        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}
