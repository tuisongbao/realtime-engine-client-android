package com.tuisongbao.engine.demo.chat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessage.TYPE;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.utils.ToolUtils;

import java.util.List;

public class ChatConversationsAdapter extends BaseAdapter {
    private static final String TAG = "TSB" + "com.tuisongbao.android.engine.chat.ChatConversationsAdapter";
    private Context mContext;
    private List<ChatConversation> mListConversation;

    public ChatConversationsAdapter(List<ChatConversation> listConversation, Context context) {
        mListConversation = listConversation;
        mContext = context;
    }

    public void refresh(List<ChatConversation> listConversation) {
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

    @SuppressLint("NewApi")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.list_item_conversation, null);
        }
        final ChatConversation conversation = mListConversation.get(position);

        // Unread message Count
        TextView unreadCountTextView = (TextView) convertView
                .findViewById(R.id.list_item_conversation_unread);
        int unreadCount = conversation.getUnreadMessageCount();
        String showNumber = "(" + unreadCount + ")";
        if (unreadCount > 0) {
            unreadCountTextView.setTextColor(mContext.getResources().getColor(R.color.red));
            if (unreadCount > 99) {
                showNumber = "(99+)";
            }
        } else {
            unreadCountTextView.setTextColor(mContext.getResources().getColor(R.color.black));
        }
        unreadCountTextView.setText(showNumber);
        unreadCountTextView.setTextSize(20);

        // Target info
        String target = conversation.getTarget();
        if (conversation.getType() == ChatType.GroupChat) {
            // TODO: query group name from demo app server.
            target = conversation.getTarget();
        }
        TextView targetTextView = (TextView) convertView
                .findViewById(R.id.list_item_conversation_target);
        targetTextView.setText("" + target);
        targetTextView.setTextSize(20);

        // last update time
        TextView lastUpdateTimeTextView = (TextView)convertView.findViewById(R.id.list_item_conversation_last_update_time);
        lastUpdateTimeTextView.setText(ToolUtils.getDisplayTime(conversation.getLastActiveAt()));

        // Show the latest message
        final TextView messageTextView = (TextView) convertView
                .findViewById(R.id.list_item_conversation_latest_message);
        conversation.getMessages(null, null, 1, new TSBEngineCallback<List<ChatMessage>>() {

            @Override
            public void onSuccess(List<ChatMessage> t) {
                if (t != null && t.size() > 0) {
                    final ChatMessage message = t.get(0);
                    ((FragmentActivity)mContext).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            String simplifiedMessage = message.getText();
                            TYPE messageType = message.getBody().getType();
                            if (messageType == TYPE.IMAGE) {
                                simplifiedMessage = "[图片]";
                            } else if (messageType == TYPE.VOICE) {
                                simplifiedMessage = "[语音]";
                            } else if (messageType == TYPE.EVENT) {
                                simplifiedMessage = ToolUtils.getEventMessage(message);
                            }
                            messageTextView.setText(simplifiedMessage);
                            messageTextView.setTextColor(mContext.getResources().getColor(R.color.gray));
                        }
                    });
                }
            }

            @Override
            public void onError(int code, String message) {

            }
        });

        return convertView;
    }
}
