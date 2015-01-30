package com.tuisongbao.android.engine.demo.chat.adapter;

import java.util.List;

import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupUser;
import com.tuisongbao.android.engine.demo.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChatGroupDetailAdapter extends BaseAdapter {

    private Context mContext;
    private List<TSBChatConversation> mListConversation;

    public ChatGroupDetailAdapter(List<TSBChatConversation> listConversation,
            Context context) {
        mListConversation = listConversation;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mListConversation == null ? 0 : mListConversation.size();
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
                    R.layout.list_item_chat_detail, null);
        }
        TSBChatConversation conversation = mListConversation.get(position);
        RelativeLayout layoutSend = (RelativeLayout) convertView
                .findViewById(R.id.list_item_chat_detail_send);
        RelativeLayout layoutReplay = (RelativeLayout) convertView
                .findViewById(R.id.list_item_chat_detail_reply);
        if ("send".equals(conversation.getType())) {

            layoutSend.setVisibility(View.VISIBLE);
            layoutReplay.setVisibility(View.GONE);

            TextView mTextViewTime = (TextView) convertView
                    .findViewById(R.id.list_item_chat_detail_send_time);
            mTextViewTime.setText(conversation.getLastActiveAt());

            TextView mTextViewContent = (TextView) convertView
                    .findViewById(R.id.list_item_chat_detail_send_content);
            mTextViewContent.setText(conversation.getTarget());

        } else {
            layoutSend.setVisibility(View.GONE);
            layoutReplay.setVisibility(View.VISIBLE);

            TextView mTextViewTime = (TextView) convertView
                    .findViewById(R.id.list_item_chat_detail_reply_time);
            mTextViewTime.setText(conversation.getLastActiveAt());

            TextView mTextViewContent = (TextView) convertView
                    .findViewById(R.id.list_item_chat_detail_reply_content);
            mTextViewContent.setText(conversation.getTarget());
        }

        return convertView;
    }
}
