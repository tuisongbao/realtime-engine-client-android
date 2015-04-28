package com.tuisongbao.android.engine.demo.chat.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
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
import com.tuisongbao.android.engine.demo.chat.cache.LoginChache;
import com.tuisongbao.android.engine.util.StrUtil;

public class ChatGroupDetailAdapter extends BaseAdapter {

    private static final String TAG = "com.tuisongbao.android.engine.chat.ChatGroupDetailAdapter";
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

            showContent(message, convertView, R.id.list_item_chat_detail_send_content, R.id.list_item_chat_detail_send_content_image);

        } else {
            layoutSend.setVisibility(View.GONE);
            layoutReplay.setVisibility(View.VISIBLE);

            TextView mTextViewReplyUser = (TextView) convertView
                    .findViewById(R.id.list_item_chat_detail_reply_user);
            mTextViewReplyUser.setText(message.getFrom());

            TextView mTextViewTime = (TextView) convertView
                    .findViewById(R.id.list_item_chat_detail_reply_time);
            mTextViewTime.setText(message.getCreatedAt());

            showContent(message, convertView, R.id.list_item_chat_detail_reply_content, R.id.list_item_chat_detail_reply_content_image);
        }

        return convertView;
    }

    private void showContent(final TSBMessage message, View contentView, int textViewId, int imageViewId) {
        if (message.getBody().getType() == TYPE.TEXT) {
            TextView mTextViewContent = (TextView) contentView
                    .findViewById(textViewId);
            mTextViewContent.setText(message.getBody() != null ? message.getBody().getText() : "");

        } else if (message.getBody().getType() == TYPE.IMAGE) {
            try {
                final ImageView mImageView = (ImageView) contentView.findViewById(imageViewId);
                message.downloadResource(new TSBEngineCallback<TSBMessage>() {

                    @Override
                    public void onSuccess(TSBMessage message) {
                        Log.d(TAG, "Image file path: " + message.getResourcePath());
                        Bitmap bmp = BitmapFactory.decodeFile(message.getResourcePath());
                        mImageView.setImageBitmap(bmp);
                    }

                    @Override
                    public void onError(int code, String message) {
                        Log.e(TAG, message);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
