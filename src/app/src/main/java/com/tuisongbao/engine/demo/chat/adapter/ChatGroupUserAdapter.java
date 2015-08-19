package com.tuisongbao.engine.demo.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tuisongbao.engine.chat.group.entity.ChatGroupUser;
import com.tuisongbao.engine.demo.R;

import java.util.List;

public class ChatGroupUserAdapter extends BaseAdapter {

    private Context mContext;
    private List<ChatGroupUser> mListGroupUser;

    public ChatGroupUserAdapter(List<ChatGroupUser> listGroupUser,
            Context context) {
        mListGroupUser = listGroupUser;
        mContext = context;
    }

    public void refresh(List<ChatGroupUser> listGroupUser) {
        mListGroupUser = listGroupUser;
        notifyDataSetChanged();
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
        ChatGroupUser user = mListGroupUser.get(position);
        mTextViewName.setText(user.getUserId() + " 状态：" + user.getPresence().getName());

        return convertView;
    }
}
