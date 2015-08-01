package com.tuisongbao.engine.demo.chat.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
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

import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessage.TYPE;
import com.tuisongbao.engine.chat.message.entity.ChatMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatVideoMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatVoiceMessageBody;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.callback.TSBProgressCallback;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.chat.ChatConversationActivity;
import com.tuisongbao.engine.demo.chat.cache.LoginCache;
import com.tuisongbao.engine.demo.chat.media.ChatVideoPlayerActivity;
import com.tuisongbao.engine.demo.chat.media.ChatVoicePlayer;
import com.tuisongbao.engine.demo.chat.media.ChatVoicePlayer.OnErrorListener;
import com.tuisongbao.engine.demo.chat.media.ChatVoicePlayer.OnStopListener;
import com.tuisongbao.engine.demo.utils.ToolUtils;
import com.tuisongbao.engine.util.StrUtil;

public class ChatMessagesAdapter extends BaseAdapter {
    private static final String TAG = "com.tuisongbao.android.engine.chat.ChatMessagesAdapter";
    private Context mContext;
    private List<ChatMessage> mMessageList;

    public ChatMessagesAdapter(List<ChatMessage> listConversation,
            Context context) {
        mMessageList = listConversation;
        mContext = context;
    }

    public void refresh(List<ChatMessage> listConversation) {
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
            ChatMessage message = mMessageList.get(position);

            RelativeLayout layoutSend = (RelativeLayout) convertView
                    .findViewById(R.id.list_item_chat_detail_send);
            RelativeLayout layoutReplay = (RelativeLayout) convertView
                    .findViewById(R.id.list_item_chat_detail_reply);
            TextView eventMessageTextView = (TextView)convertView.findViewById(R.id.list_item_chat_event_message);

            // Handle event message
            if (message.getBody().getType() == TYPE.EVENT) {
                layoutSend.setVisibility(View.GONE);
                layoutReplay.setVisibility(View.GONE);
                eventMessageTextView.setVisibility(View.VISIBLE);

                eventMessageTextView.setText(ToolUtils.getEventMessage(message));
                return convertView;
            }

            eventMessageTextView.setVisibility(View.GONE);

            // Handle chat message
            boolean sentByLoginUser = StrUtil.strNotNull(message.getFrom()).equals(LoginCache.getUserId());
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

    private void displayMessageContent(ChatMessage message, View convertView, boolean sentByLoginUser) {
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

        ChatMessageBody messageBody = message.getBody();
        if (messageBody.getType() == TYPE.TEXT) {
            textView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            voiceButton.setVisibility(View.GONE);

            textView.setText(messageBody != null ? message.getText() : "");
            textView.setTextSize(17);

        } else if (messageBody.getType() == TYPE.IMAGE) {
            imageView.setVisibility(View.VISIBLE);
            voiceButton.setVisibility(View.GONE);

            showImageMessage(message, convertView, imageView, textView);

        } else if (messageBody.getType() == TYPE.VOICE) {
            textView.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            voiceButton.setVisibility(View.VISIBLE);

            showVoiceMessage(message, convertView, voiceButton);
        } else if (messageBody.getType() == TYPE.VIDEO) {
            textView.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            voiceButton.setVisibility(View.VISIBLE);

            showVideoWidget(message, convertView, voiceButton);
        }
    }

    private void showVoiceMessage(final ChatMessage message, View convertView, final Button voiceButton) {
        ChatVoiceMessageBody body = (ChatVoiceMessageBody)message.getBody();
        final String duration = body.getDuration();
        voiceButton.setText("voice: " + duration);

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

                ChatVoicePlayer player = ChatVoicePlayer.getInstance();
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
                }, new TSBProgressCallback() {

                    @Override
                    public void progress(final int percent) {
                        ((ChatConversationActivity)mContext).runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                voiceButton.setText(percent + "%");
                                if (percent == 100) {
                                    voiceButton.setText(duration);
                                }
                            }
                        });
                    }
                });
            }
        };
        voiceButton.setOnClickListener(listener);
    }

    private void showImageMessage(final ChatMessage message, final View contentView, final ImageView imageView, final TextView textView) {
        message.downloadResource(new TSBEngineCallback<ChatMessage>() {

            @Override
            public void onSuccess(final ChatMessage message) {
                ((ChatConversationActivity)mContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Bitmap bmp = BitmapFactory.decodeFile(message.getResourcePath());
                        imageView.setImageBitmap(bmp);
                        imageView.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(int code, String message) {
                ((ChatConversationActivity)mContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        textView.setText("Failed to load image");
                    }
                });
            }
        }, new TSBProgressCallback() {

            @Override
            public void progress(final int percent) {
                ((ChatConversationActivity)mContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        textView.setText(percent + "%");
                    }
                });
            }
        });
    }

    private void showVideoWidget(final ChatMessage message, View convertView, final Button voiceButton) {
        final ChatVideoMessageBody body = (ChatVideoMessageBody)message.getBody();
        final String duration = body.getDuration();
        voiceButton.setText("video: " + duration);

        voiceButton.setTag("idle");
        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                message.downloadResource(new TSBEngineCallback<ChatMessage>() {

                    @Override
                    public void onSuccess(ChatMessage t) {
                        Intent intent = new Intent(mContext.getApplicationContext(), ChatVideoPlayerActivity.class);
                        intent.putExtra(ChatVideoPlayerActivity.EXTRA_VIDEO_PATH, message.getResourcePath());
                        mContext.startActivity(intent);
                    }

                    @Override
                    public void onError(int code, String message) {

                    }
                }, new TSBProgressCallback() {

                    @Override
                    public void progress(final int percent) {
                        ((ChatConversationActivity)mContext).runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                voiceButton.setText(percent + "%");
                                if (percent == 100) {
                                    voiceButton.setText(duration);
                                }
                            }
                        });
                    }
                });
            }
        };
        voiceButton.setOnClickListener(listener);
    }
}
