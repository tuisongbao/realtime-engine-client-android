package com.tuisongbao.android.engine.demo.chat.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.cache.LoginChache;
import com.tuisongbao.android.engine.util.StrUtil;

public class ChatGroupDetailAdapter extends BaseAdapter {

    private Context mContext;
    private List<TSBMessage> mListConversation;

    public ChatGroupDetailAdapter(List<TSBMessage> listConversation,
            Context context) {
        mListConversation = listConversation;
        mContext = context;
    }
    
    public void refresh(List<TSBMessage> listConversation) {
        mListConversation = listConversation;
        notifyDataSetChanged();
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
        TSBMessage message = mListConversation.get(position);
        RelativeLayout layoutSend = (RelativeLayout) convertView
                .findViewById(R.id.list_item_chat_detail_send);
        RelativeLayout layoutReplay = (RelativeLayout) convertView
                .findViewById(R.id.list_item_chat_detail_reply);
        if (StrUtil.strNotNull(message.getFrom()).equals(LoginChache.getUserId())) {

            layoutSend.setVisibility(View.VISIBLE);
            layoutReplay.setVisibility(View.GONE);

            TextView mTextViewTime = (TextView) convertView
                    .findViewById(R.id.list_item_chat_detail_send_time);
            mTextViewTime.setText(message.getCreatedAt());

            TextView mTextViewContent = (TextView) convertView
                    .findViewById(R.id.list_item_chat_detail_send_content);
            mTextViewContent.setText(message.getBody() != null ? message.getBody().getText() : "");

        } else {
            layoutSend.setVisibility(View.GONE);
            layoutReplay.setVisibility(View.VISIBLE);

            TextView mTextViewReplyUser = (TextView) convertView
                    .findViewById(R.id.list_item_chat_detail_reply_user);
            mTextViewReplyUser.setText(message.getFrom());

            TextView mTextViewTime = (TextView) convertView
                    .findViewById(R.id.list_item_chat_detail_reply_time);
            mTextViewTime.setText(message.getCreatedAt());

            TextView mTextViewContent = (TextView) convertView
                    .findViewById(R.id.list_item_chat_detail_reply_content);
            mTextViewContent.setText(message.getBody() != null ? message.getBody().getText() : "");
        }

        return convertView;
    }
}
