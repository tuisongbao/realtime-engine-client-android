package com.tuisongbao.android.engine.demo.chat.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.entity.TSBMessage.TYPE;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.demo.R;
import com.tuisongbao.android.engine.demo.chat.ChatConversationActivity;
import com.tuisongbao.android.engine.demo.chat.cache.LoginChache;
import com.tuisongbao.android.engine.demo.chat.utils.ToolUtils;
import com.tuisongbao.android.engine.util.StrUtil;

public class ChatMessagesAdapter extends BaseAdapter {

    private static final String TAG = "com.tuisongbao.android.engine.chat.ChatMessagesAdapter";
    private Context mContext;
    private List<TSBMessage> mMessageList;

    public ChatMessagesAdapter(List<TSBMessage> listConversation,
            Context context) {
        mMessageList = listConversation;
        mContext = context;
    }

    public void refresh(List<TSBMessage> listConversation) {
        mMessageList = listConversation;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMessageList == null ? 0 : mMessageList.size();
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
        TSBMessage message = mMessageList.get(position);
        RelativeLayout layoutSend = (RelativeLayout) convertView
                .findViewById(R.id.list_item_chat_detail_send);
        RelativeLayout layoutReplay = (RelativeLayout) convertView
                .findViewById(R.id.list_item_chat_detail_reply);
        if (StrUtil.strNotNull(message.getFrom()).equals(LoginChache.getUserId())) {

            layoutSend.setVisibility(View.VISIBLE);
            layoutReplay.setVisibility(View.GONE);

            TextView mTextViewTime = (TextView) convertView
                    .findViewById(R.id.list_item_chat_detail_send_time);
            mTextViewTime.setText(ToolUtils.getDisplayTime(message.getCreatedAt()));

            showContent(message, convertView, R.id.list_item_chat_detail_send_content, R.id.list_item_chat_detail_send_content_image);

        } else {
            layoutSend.setVisibility(View.GONE);
            layoutReplay.setVisibility(View.VISIBLE);

            TextView mTextViewReplyUser = (TextView) convertView
                    .findViewById(R.id.list_item_chat_detail_reply_user);
            mTextViewReplyUser.setText(message.getFrom());

            TextView mTextViewTime = (TextView) convertView
                    .findViewById(R.id.list_item_chat_detail_reply_time);
            mTextViewTime.setText(ToolUtils.getDisplayTime(message.getCreatedAt()));

            showContent(message, convertView, R.id.list_item_chat_detail_reply_content, R.id.list_item_chat_detail_reply_content_image);
        }

        return convertView;
    }

    private void showContent(final TSBMessage message, final View contentView, final int textViewId, final int imageViewId) {
        if (message.getBody().getType() == TYPE.TEXT) {
            // clear imageView, as the list item is re-use, the imageView will not be cleared
            ImageView imageView = (ImageView) contentView.findViewById(imageViewId);
            imageView.setVisibility(View.GONE);

            TextView textViewContent = (TextView) contentView
                    .findViewById(textViewId);
            textViewContent.setText(message.getBody() != null ? message.getText() : "");
            textViewContent.setTextSize(17);
            textViewContent.setVisibility(View.VISIBLE);

        } else if (message.getBody().getType() == TYPE.IMAGE) {
            try {
                // clear textView
                TextView textView = (TextView) contentView.findViewById(textViewId);
                textView.setVisibility(View.GONE);

                // Load image is slow, sometimes it will show the old image of the reused item, so hide imageView first
                ImageView imageView = (ImageView) contentView.findViewById(imageViewId);
                imageView.setVisibility(View.GONE);

                message.downloadResource(new TSBEngineCallback<TSBMessage>() {

                    @Override
                    public void onSuccess(final TSBMessage message) {
                        ((ChatConversationActivity)mContext).runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                final ImageView imageView = (ImageView) contentView.findViewById(imageViewId);
                                Bitmap bmp = BitmapFactory.decodeFile(message.getResourcePath());
                                imageView.setImageBitmap(bmp);
                                imageView.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    @Override
                    public void onError(int code, String message) {
                        ((ChatConversationActivity)mContext).runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                TextView textViewContent = (TextView) contentView
                                        .findViewById(textViewId);
                                textViewContent.setText("Failed to load image");
                                textViewContent.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
