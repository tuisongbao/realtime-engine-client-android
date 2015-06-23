package com.tuisongbao.android.engine.demo.chat.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.entity.TSBMessage.TYPE;
import com.tuisongbao.android.engine.chat.entity.TSBVoiceMessageBody;
import com.tuisongbao.android.engine.chat.media.TSBMediaPlayer;
import com.tuisongbao.android.engine.chat.media.TSBMediaPlayer.OnErrorListener;
import com.tuisongbao.android.engine.chat.media.TSBMediaPlayer.OnStopListener;
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
                    R.layout.list_item_message, null);
        }
        try {
            TSBMessage message = mMessageList.get(position);

            RelativeLayout layoutSend = (RelativeLayout) convertView
                    .findViewById(R.id.list_item_chat_detail_send);
            RelativeLayout layoutReplay = (RelativeLayout) convertView
                    .findViewById(R.id.list_item_chat_detail_reply);

            boolean sentByLoginUser = StrUtil.strNotNull(message.getFrom()).equals(LoginChache.getUserId());
            if (sentByLoginUser) {

                layoutSend.setVisibility(View.VISIBLE);
                layoutReplay.setVisibility(View.GONE);

                TextView mTextViewTime = (TextView) convertView
                        .findViewById(R.id.list_item_chat_detail_send_time);
                mTextViewTime.setText(ToolUtils.getDisplayTime(message.getCreatedAt()));
            } else {
                layoutSend.setVisibility(View.GONE);
                layoutReplay.setVisibility(View.VISIBLE);

                TextView mTextViewReplyUser = (TextView) convertView
                        .findViewById(R.id.list_item_chat_detail_reply_user);
                mTextViewReplyUser.setText(message.getFrom());

                TextView mTextViewTime = (TextView) convertView
                        .findViewById(R.id.list_item_chat_detail_reply_time);
                mTextViewTime.setText(ToolUtils.getDisplayTime(message.getCreatedAt()));
            }
            displayMessageContent(message, convertView, sentByLoginUser);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    private void displayMessageContent(TSBMessage message, View convertView, boolean sentByLoginUser) {
        TextView textView = null;
        ImageView imageView = null;
        Button voiceButton = null;

        if (sentByLoginUser) {
            textView = (TextView) convertView.findViewById(R.id.list_item_chat_detail_send_content);
            imageView = (ImageView) convertView.findViewById(R.id.list_item_chat_detail_send_content_image);
            voiceButton = (Button) convertView.findViewById(R.id.list_item_chat_detail_send_content_voice);
        } else {
            textView = (TextView) convertView.findViewById(R.id.list_item_chat_detail_reply_content);
            imageView = (ImageView) convertView.findViewById(R.id.list_item_chat_detail_reply_content_image);
            voiceButton = (Button) convertView.findViewById(R.id.list_item_chat_detail_reply_content_voice);
        }

        if (message.getBody().getType() == TYPE.TEXT) {
            textView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            voiceButton.setVisibility(View.GONE);

            textView.setText(message.getBody() != null ? message.getText() : "");
            textView.setTextSize(17);

        } else if (message.getBody().getType() == TYPE.IMAGE) {
            textView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            voiceButton.setVisibility(View.GONE);

            showImageMessage(message, convertView, imageView, textView);

        } else if (message.getBody().getType() == TYPE.VOICE) {
            textView.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            voiceButton.setVisibility(View.VISIBLE);

            showVoiceMessage(message, convertView, voiceButton);
        }
    }

    private void showVoiceMessage(final TSBMessage message, View convertView, final Button voiceButton) {
        TSBVoiceMessageBody body = (TSBVoiceMessageBody)message.getBody();
        JsonObject mediaInfo = body.getMediaInfo();
        int duration = (int)mediaInfo.get(TSBVoiceMessageBody.VOICE_INFO_DURATION).getAsDouble();
        voiceButton.setText(duration + "'");

        // TODO: set different width measured by duration.

        voiceButton.setTag("idle");
        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.d(TAG, "onClick");
                String status = voiceButton.getTag().toString();
                if (status == "playing") {
                    return;
                }

                voiceButton.setBackgroundColor(mContext.getResources().getColor(R.color.blue));
                voiceButton.setTag("playing");

                TSBMediaPlayer player = TSBMediaPlayer.getInstance();
                player.start(message, new OnStopListener() {

                    @Override
                    public void onStop() {
                        Log.d(TAG, "onStop");
                        voiceButton.setTag("idle");
                        ((ChatConversationActivity)mContext).runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                voiceButton.setBackgroundColor(mContext.getResources().getColor(R.color.gray));

                            }
                        });
                    }
                }, new OnErrorListener() {

                    @Override
                    public void onError(String error) {
                        ((ChatConversationActivity)mContext).runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                voiceButton.setBackgroundColor(mContext.getResources().getColor(R.color.red));
                                voiceButton.setEnabled(false);
                            }
                        });
                    }
                });
            }
        };
        voiceButton.setOnClickListener(listener);
    }

    private void showImageMessage(final TSBMessage message, final View contentView, final ImageView imageView, final TextView textView) {
        message.downloadResource(new TSBEngineCallback<TSBMessage>() {

            @Override
            public void onSuccess(final TSBMessage message) {
                ((ChatConversationActivity)mContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
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
                        textView.setText("Failed to load image");
                        textView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }
}
