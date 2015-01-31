package com.tuisongbao.android.engine.demo.chat.adapter;

import java.util.List;

import com.tuisongbao.android.engine.chat.entity.TSBChatGroupUser;
import com.tuisongbao.android.engine.demo.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatListFriendAdapter extends BaseAdapter {

    private Context mContext;
    private List<TSBChatGroupUser> mListUser;

    public ChatListFriendAdapter(List<TSBChatGroupUser> listUser, Context context) {
        mListUser = listUser;
        mContext = context;
    }
    
    public void refresh(List<TSBChatGroupUser> listUser) {
        mListUser = listUser;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mListUser == null ? 0 :mListUser.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.list_item_chat_list_friend, null);
        }
        TextView mTextViewName = (TextView) convertView
                .findViewById(R.id.list_item_chat_list_friend_name);
        mTextViewName.setText(mListUser.get(position).getUserId());

        return convertView;
    }
}
