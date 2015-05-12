package com.tuisongbao.android.engine.demo.chat.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.demo.R;

public class ChatConversationsAdapter extends BaseAdapter {

    private Context mContext;
    private List<TSBChatConversation> mListConversation;

    public ChatConversationsAdapter(List<TSBChatConversation> listConversation, Context context) {
        mListConversation = listConversation;
        mContext = context;
    }
    
    public void refresh(List<TSBChatConversation> listConversation) {
        mListConversation = listConversation;
        notifyDataSetChanged();
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
        mTextViewName.setText("未读消息数：" + mListConversation.get(position).getUnreadMessageCount());
        
        TextView mTextViewContent = (TextView) convertView
                .findViewById(R.id.list_item_chat_talk_content);
        mTextViewContent.setText("和(group/user id)：" + mListConversation.get(position).getTarget());
        
        TextView mTextViewType = (TextView) convertView
                .findViewById(R.id.list_item_chat_talk_type);
        mTextViewType.setText("会话类型：" + mListConversation.get(position).getType().getName());
        
        TextView mTextViewTime = (TextView) convertView
                .findViewById(R.id.list_item_chat_talk_time);
        mTextViewTime.setText("时间：" + mListConversation.get(position).getLastActiveAt());

        return convertView;
    }
}
