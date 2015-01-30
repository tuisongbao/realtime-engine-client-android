package com.tuisongbao.android.engine.demo.chat.adapter;

import java.util.List;

import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupUser;
import com.tuisongbao.android.engine.demo.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatGroupAdapter extends BaseAdapter {

    private Context mContext;
    private List<TSBChatGroup> mListGroup;

    public ChatGroupAdapter(List<TSBChatGroup> listGroup, Context context) {
        mListGroup = listGroup;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mListGroup == null ? 0 : mListGroup.size();
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
                    R.layout.list_item_chat_group, null);
        }
        TextView mTextViewName = (TextView) convertView
                .findViewById(R.id.list_item_chat_group_name);
        mTextViewName.setText(mListGroup.get(position).getName());

        return convertView;
    }
}
