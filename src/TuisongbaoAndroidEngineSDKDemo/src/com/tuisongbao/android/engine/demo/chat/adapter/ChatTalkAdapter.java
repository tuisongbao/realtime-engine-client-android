package com.tuisongbao.android.engine.demo.chat.adapter;

import java.util.List;

import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupUser;
import com.tuisongbao.android.engine.demo.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatTalkAdapter extends BaseAdapter {

    private Context mContext;
    private List<TSBChatConversation> mListConversation;

    public ChatTalkAdapter(List<TSBChatConversation> listConversation, Context context) {
        mListConversation = listConversation;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mListConversation == null ? 0 :mListConversation.size();
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
                    R.layout.list_item_chat_talk, null);
        }
        TextView mTextViewName = (TextView) convertView
                .findViewById(R.id.list_item_chat_talk_title);
        mTextViewName.setText(""+mListConversation.get(position).getUnreadMessageCount());
        
        TextView mTextViewContent = (TextView) convertView
                .findViewById(R.id.list_item_chat_talk_content);
        mTextViewContent.setText(mListConversation.get(position).getTarget());
        
        TextView mTextViewTime = (TextView) convertView
                .findViewById(R.id.list_item_chat_talk_time);
        mTextViewTime.setText(mListConversation.get(position).getLastActiveAt());

        return convertView;
    }
}
