package com.tuisongbao.android.engine.demo.chat.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tuisongbao.android.engine.chat.entity.TSBChatGroupUser;
import com.tuisongbao.android.engine.demo.R;

public class ChatGroupUserAdapter extends BaseAdapter {

    private Context mContext;
    private List<TSBChatGroupUser> mListGroupUser;

    public ChatGroupUserAdapter(List<TSBChatGroupUser> listGroupUser,
            Context context) {
        mListGroupUser = listGroupUser;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mListGroupUser == null ? 0 : mListGroupUser.size();
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
                    R.layout.list_item_chat_group_member, null);
        }
        TextView mTextViewName = (TextView) convertView
                .findViewById(R.id.list_item_chat_group_member_name);
        mTextViewName.setText(mListGroupUser.get(position).getUserId());

        return convertView;
    }
}
