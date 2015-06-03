package com.tuisongbao.android.engine.demo.chat.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tuisongbao.android.engine.chat.entity.ChatType;
import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.entity.TSBMessage.TYPE;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.entity.ConversationWrapper;
import com.tuisongbao.android.engine.demo.chat.utils.ToolUtils;

public class ChatConversationsAdapter extends BaseAdapter {
    private static final String TAG = "com.tuisongbao.android.engine.chat.ChatConversationsAdapter";
    private Context mContext;
    private List<ConversationWrapper> mListConversation;

    public ChatConversationsAdapter(List<ConversationWrapper> listConversation, Context context) {
        mListConversation = listConversation;
        mContext = context;
    }

    public void refresh(List<ConversationWrapper> listConversation) {
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
        final ConversationWrapper wrapper = mListConversation.get(position);
        final TSBChatConversation tsbConversation = wrapper.getConversation();

        // Unread message Count
        TextView unreadCountTextView = (TextView) convertView
                .findViewById(R.id.list_item_conversation_unread);
        int unreadCount = tsbConversation.getUnreadMessageCount() + wrapper.localUnreadCount;
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
        String target = tsbConversation.getTarget();
        if (tsbConversation.getType() == ChatType.GroupChat) {
            target = tsbConversation.getGroupName();
        }
        TextView targetTextView = (TextView) convertView
                .findViewById(R.id.list_item_conversation_target);
        targetTextView.setText("" + target);
        targetTextView.setTextSize(20);

        // last update time
        TextView lastUpdateTimeTextView = (TextView)convertView.findViewById(R.id.list_item_conversation_last_update_time);
        lastUpdateTimeTextView.setText(ToolUtils.getDisplayTime(tsbConversation.getLastActiveAt()));

        // Show the latest message
        final TextView messageTextView = (TextView) convertView
                .findViewById(R.id.list_item_conversation_latest_message);
        tsbConversation.getMessages(null, null, 1, new TSBEngineCallback<List<TSBMessage>>() {

            @Override
            public void onSuccess(List<TSBMessage> t) {
                if (t != null && t.size() > 0) {
                    final TSBMessage message = t.get(0);
                    ((FragmentActivity)mContext).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            String simplifiedMessage = message.getText();
                            if (message.getBody().getType() == TYPE.IMAGE) {
                                simplifiedMessage = "[图片]";
                            } else if (message.getBody().getType() == TYPE.VOICE) {
                                simplifiedMessage = "[语音]";
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
