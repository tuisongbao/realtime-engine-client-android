package com.tuisongbao.engine.demo.chat.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.google.gson.JsonObject;
import com.tuisongbao.engine.chat.message.ChatMessage;
import com.tuisongbao.engine.chat.message.ChatMessage.TYPE;
import com.tuisongbao.engine.chat.message.ChatMessageContent;
import com.tuisongbao.engine.chat.message.ChatMessageImageContent;
import com.tuisongbao.engine.chat.message.ChatMessageVideoContent;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.bean.MessageStatus;
import com.tuisongbao.engine.demo.chat.ChatVideoPlayerActivity;
import com.tuisongbao.engine.demo.chat.ChatVideoPlayerActivity_;
import com.tuisongbao.engine.demo.chat.utils.ImageCache;
import com.tuisongbao.engine.demo.common.Utils;
import com.tuisongbao.engine.demo.net.NetClient;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by user on 15-9-1.
 */
public class MessageAdapter extends BaseAdapter {

    private final static String TAG = "msg";

    private static final int MESSAGE_TYPE_RECV_TXT = 0;
    private static final int MESSAGE_TYPE_SENT_TXT = 1;
    private static final int MESSAGE_TYPE_SENT_IMAGE = 2;
    private static final int MESSAGE_TYPE_SENT_LOCATION = 3;
    private static final int MESSAGE_TYPE_RECV_LOCATION = 4;
    private static final int MESSAGE_TYPE_RECV_IMAGE = 5;
    private static final int MESSAGE_TYPE_SENT_VOICE = 6;
    private static final int MESSAGE_TYPE_RECV_VOICE = 7;
    private static final int MESSAGE_TYPE_SENT_VIDEO = 8;
    private static final int MESSAGE_TYPE_RECV_VIDEO = 9;
    private static final int MESSAGE_TYPE_EVENT = 10;
    private static final int MESSAGE_TYPE_RECV_FILE = 11;
    private static final int MESSAGE_TYPE_SENT_VOICE_CALL = 12;
    private static final int MESSAGE_TYPE_RECV_VOICE_CALL = 13;
    private static final int MESSAGE_TYPE_SENT_VIDEO_CALL = 14;
    private static final int MESSAGE_TYPE_RECV_VIDEO_CALL = 15;
    private LayoutInflater inflater;

    private Activity activity;
    private Context context;

    private Map<String, Timer> timers = new Hashtable<String, Timer>();

    List<ChatMessage> chatMessages;
    private String target;

    public MessageAdapter(Context context, String target, final List<ChatMessage> mMessageList) {
        this.context = context;
        this.target = target;
        activity = (Activity) context;
        this.chatMessages = mMessageList;
        if (chatMessages == null) {
            chatMessages = new ArrayList<>();
        }
        inflater = LayoutInflater.from(context);
    }

    public void setChatMessages(List<ChatMessage> chatMessages) {
        Collections.sort(chatMessages, new Comparator<ChatMessage>() {
            @Override
            public int compare(ChatMessage lhs, ChatMessage rhs) {
                return (int) (lhs.getMessageId() - rhs.getMessageId());
            }
        });
        this.chatMessages = chatMessages;
    }

    /**
     * 获取item数
     */
    public int getCount() {
        return chatMessages.size();
    }

    /**
     * 刷新页面
     */
    public void refresh() {
        notifyDataSetChanged();
    }

    public ChatMessage getItem(int position) {
        return chatMessages.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    private boolean isNotByMe(ChatMessage chatMessage) {
        return !chatMessage.getFrom().equals(App.getInstance2().getChatUser().getUserId());
    }

    /**
     * 获取item类型
     */
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        if (message.getContent().getType() == TYPE.TEXT) {
            return isNotByMe(message) ? MESSAGE_TYPE_RECV_TXT
                    : MESSAGE_TYPE_SENT_TXT;
        }
        if (message.getContent().getType() == TYPE.IMAGE) {
            return isNotByMe(message) ? MESSAGE_TYPE_RECV_IMAGE
                    : MESSAGE_TYPE_SENT_IMAGE;

        }
        if (message.getContent().getType() == TYPE.LOCATION) {
            return isNotByMe(message) ? MESSAGE_TYPE_RECV_LOCATION
                    : MESSAGE_TYPE_SENT_LOCATION;
        }
        if (message.getContent().getType() == TYPE.VOICE) {
            return isNotByMe(message) ? MESSAGE_TYPE_RECV_VOICE
                    : MESSAGE_TYPE_SENT_VOICE;
        }
        if (message.getContent().getType() == TYPE.VIDEO) {
            return isNotByMe(message) ? MESSAGE_TYPE_RECV_VIDEO
                    : MESSAGE_TYPE_SENT_VIDEO;
        }
        if (message.getContent().getType() == TYPE.EVENT) {
            return MESSAGE_TYPE_EVENT;

        }

        return -1;// invalid
    }

    public int getViewTypeCount() {
        return 16;
    }

    private View createViewByMessage(ChatMessage message, int position) {
        switch (message.getContent().getType()) {
            case LOCATION:
                return isNotByMe(message) ? inflater
                        .inflate(R.layout.row_received_location, null) : inflater
                        .inflate(R.layout.row_sent_location, null);
            case IMAGE:
                return isNotByMe(message) ? inflater
                        .inflate(R.layout.row_received_picture, null) : inflater
                        .inflate(R.layout.row_sent_picture, null);

            case VOICE:
                return isNotByMe(message) ? inflater
                        .inflate(R.layout.row_received_voice, null) : inflater
                        .inflate(R.layout.row_sent_voice, null);
            case VIDEO:
                return isNotByMe(message) ? inflater
                        .inflate(R.layout.row_received_video, null) : inflater
                        .inflate(R.layout.row_sent_video, null);
            case EVENT:
                return inflater.inflate(R.layout.row_event, null);
            default:
                return isNotByMe(message) ? inflater
                        .inflate(R.layout.row_received_message, null) : inflater
                        .inflate(R.layout.row_sent_message, null);
        }
    }

    @SuppressLint("NewApi")
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ChatMessage message = getItem(position);
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = createViewByMessage(message, position);
            if (message.getContent().getType() == TYPE.IMAGE) {
                try {
                    holder.iv = ((ImageView) convertView
                            .findViewById(R.id.iv_sendPicture));
                    holder.head_iv = (ImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView
                            .findViewById(R.id.percentage);
                    holder.pb = (ProgressBar) convertView
                            .findViewById(R.id.progressBar);
                    holder.staus_iv = (ImageView) convertView
                            .findViewById(R.id.msg_status);
                    holder.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }

            } else if (message.getContent().getType() == TYPE.TEXT) {

                try {
                    holder.pb = (ProgressBar) convertView
                            .findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView
                            .findViewById(R.id.msg_status);
                    holder.head_iv = (ImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    // 这里是文字内容
                    holder.tv = (TextView) convertView
                            .findViewById(R.id.tv_chatcontent);
                    holder.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }

            } else if (message.getContent().getType() == TYPE.VOICE) {
                try {
                    holder.iv = ((ImageView) convertView
                            .findViewById(R.id.iv_voice));
                    holder.head_iv = (ImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView
                            .findViewById(R.id.tv_length);
                    holder.pb = (ProgressBar) convertView
                            .findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView
                            .findViewById(R.id.msg_status);
                    holder.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                    holder.iv_read_status = (ImageView) convertView
                            .findViewById(R.id.iv_unread_voice);
                } catch (Exception e) {
                }
            } else if (message.getContent().getType() == TYPE.LOCATION) {
                try {
                    holder.head_iv = (ImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView
                            .findViewById(R.id.tv_location);
                    holder.pb = (ProgressBar) convertView
                            .findViewById(R.id.pb_sending);
                    holder.staus_iv = (ImageView) convertView
                            .findViewById(R.id.msg_status);
                    holder.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);
                } catch (Exception e) {
                }
            } else if (message.getContent().getType() == TYPE.VIDEO) {
                try {
                    holder.iv = ((ImageView) convertView
                            .findViewById(R.id.chatting_content_iv));
                    holder.head_iv = (ImageView) convertView
                            .findViewById(R.id.iv_userhead);
                    holder.tv = (TextView) convertView
                            .findViewById(R.id.percentage);
                    holder.pb = (ProgressBar) convertView
                            .findViewById(R.id.progressBar);
                    holder.staus_iv = (ImageView) convertView
                            .findViewById(R.id.msg_status);
                    holder.size = (TextView) convertView
                            .findViewById(R.id.chatting_size_iv);
                    holder.timeLength = (TextView) convertView
                            .findViewById(R.id.chatting_length_iv);
                    holder.playBtn = (ImageView) convertView
                            .findViewById(R.id.chatting_status_btn);
                    holder.container_status_btn = (LinearLayout) convertView
                            .findViewById(R.id.container_status_btn);
                    holder.tv_userId = (TextView) convertView
                            .findViewById(R.id.tv_userid);

                } catch (Exception e) {
                }

            } else if (message.getContent().getType() == TYPE.EVENT) {
                try {
                    holder.tv = (TextView) convertView
                            .findViewById(R.id.tv_event);
                } catch (Exception e) {

                }
            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        switch (message.getContent().getType()) {
            // 根据消息type显示item
            case IMAGE: // 图片
                handleImageMessage(message, holder, position, convertView);
                break;
            case TEXT: // 文本
                handleTextMessage(message, holder, position);
                break;
            case LOCATION: // 位置
                handleLocationMessage(message, holder, position, convertView);
                break;
            case VOICE: // 语音
                handleVoiceMessage(message, holder, position, convertView);
                break;
            case VIDEO: // 视频
                handleVideoMessage(message, holder, position, convertView);
                break;
            case EVENT: // Event
                handleEventMessage(message, holder, position, convertView);
                break;
            default:
                // not supported
        }

        // 获取头像
        if (holder.head_iv != null) {
            NetClient.getIconBitmap(holder.head_iv, Constants.USERAVATARURL + message.getFrom());
        }

        // 添加时间
        TextView timestamp = (TextView) convertView
                .findViewById(R.id.timestamp);
        long previousMessageTime = 0l;

        if(chatMessages.size() > 1 && position > 0){
            previousMessageTime = chatMessages.get(position - 1).getCreatedAtInDate().getTime();
        }

        long timeInterval = message.getCreatedAtInDate().getTime() - previousMessageTime;

        // 间隔5分钟显示一次时间
        if (timestamp != null && timeInterval > 5 * 60 * 1000) {
            String time = Utils.formatDateTime(message.getCreatedAtInDate());
            timestamp.setText(time);
            timestamp.setVisibility(View.VISIBLE);
        }else{
            timestamp.setVisibility(View.GONE);
        }

        return convertView;
    }

    private void handleEventMessage(ChatMessage message, ViewHolder holder, int position, View convertView) {
        holder.tv.setText(Utils.getEventMessage(message));

    }

    private String formateDuration(double duration){
        String durationStr = duration + "";
        if(durationStr.length() > 3){
            durationStr = durationStr.substring(0, 3);
        }
        return durationStr;
    }
    
    /**
     * 文本消息
     *
     * @param message
     * @param holder
     * @param position
     */
    private void handleTextMessage(ChatMessage message, ViewHolder holder,
                                   final int position) {

        String text = message.getContent().getText();
        // 设置内容
        holder.tv.setText(text, TextView.BufferType.SPANNABLE);
        // 设置长按事件监听
        holder.tv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        if (holder.pb == null) {
            return;
        }
        holder.pb.setVisibility(View.GONE);
        holder.staus_iv.setVisibility(View.GONE);
    }


    /**
     * 图片消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleImageMessage(final ChatMessage message,
                                    final ViewHolder holder, final int position, View convertView) {
        holder.pb.setTag(position);
        holder.iv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                return true;
            }
        });

        if (isNotByMe(message)) {
            String filePath = message.getContent().getFile().getThumbnailPath();

            if (filePath != null && TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                holder.pb.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
                Bitmap bmp = BitmapFactory.decodeFile(filePath);
                holder.iv.setImageBitmap(bmp);
            } else {
                showDownloadImageProgress(message, holder);
            }
        } else {
            // 发送的消息
            // process send message
            // send pic, show the pic directly
            showImageView(holder.iv, message, true);

            handleMediaMessageStatus(holder, message);
        }
    }

    void handleMediaMessageStatus(final ViewHolder holder, final ChatMessage message) {
        int status = MessageStatus.SUCCESS.getValue();

        if (message.getContent().getExtra() != null && message.getContent().getExtra().has("status")) {
            status = message.getContent().getExtra().get("status").getAsInt();
        }


        switch (MessageStatus.getMessageStatus(status)) {
            case SUCCESS:
            case FAIL:
                if (holder.pb != null) {
                    holder.pb.setVisibility(View.GONE);
                }
                if (message.getContent().getFile().getDuration() != 0) {
                    holder.tv.setText(formateDuration(message.getContent().getFile().getDuration() ) + "\"");
                    holder.tv.setVisibility(View.VISIBLE);
                }else{
                    holder.tv.setVisibility(View.GONE);
                }
                break;
            case INPROGRESS:
                if(holder.staus_iv != null){
                    holder.staus_iv.setVisibility(View.GONE);
                }
                if (holder.pb != null) {
                    holder.pb.setVisibility(View.VISIBLE);
                } else {
                    return;
                }
                if (holder.tv != null) {
                    holder.tv.setVisibility(View.VISIBLE);
                }
                if (timers.containsKey(message.getMessageId()))
                    return;
                // set a timer
                final Timer timer = new Timer();
                timers.put(message.getMessageId() + "", timer);
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                if (message.getContent().getExtra().get("status").getAsInt() == MessageStatus.FAIL.getValue()) {
                                    holder.pb.setVisibility(View.GONE);
                                    holder.tv.setVisibility(View.GONE);
                                    holder.staus_iv.setVisibility(View.VISIBLE);
                                    String errMsg = activity.getString(R.string.send_fail) + activity
                                            .getString(R.string.connect_failuer_toast);
                                    Utils.showShortToast(activity, errMsg);
                                    timer.cancel();
                                    return;
                                }
                                holder.pb.setVisibility(View.VISIBLE);
                                holder.tv.setVisibility(View.VISIBLE);

                                if (message.getContent().getExtra() == null || !message.getContent().getExtra().has("progress")) {
                                    return;
                                }
                                int progress = message.getContent().getExtra().get("progress").getAsInt();

                                holder.tv.setText(progress + "%");
                                if (progress == 100) {
                                    JsonObject json = message.getContent().getExtra();
                                    json.addProperty("status", MessageStatus.SUCCESS.getValue());
                                    holder.pb.setVisibility(View.GONE);
                                    if(holder.staus_iv != null){
                                        holder.staus_iv.setVisibility(View.GONE);
                                    }
                                    if (message.getContent().getFile().getDuration() != 0) {
                                        holder.tv.setText(formateDuration(message.getContent().getFile().getDuration() ) + "\"");
                                        holder.tv.setVisibility(View.VISIBLE);
                                    }else{
                                        holder.tv.setVisibility(View.GONE);
                                    }
                                    timer.cancel();
                                }
                            }
                        });

                    }
                }, 0, 500);
                break;
            default:
                holder.pb.setVisibility(View.GONE);
                holder.tv.setVisibility(View.GONE);
        }
    }

    /**
     * 视频消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleVideoMessage(final ChatMessage message,
                                    final ViewHolder holder, final int position, View convertView) {

        ChatMessageContent messageContent = message.getContent();

        String localThumb = messageContent.getFile().getThumbnailPath();

        String thumbUrl = messageContent.getFile().getThumbUrl();

        LogUtils.i("video message", thumbUrl + ":" + localThumb);

        showVideoThumbView(localThumb, holder.iv, thumbUrl, message);

        if (messageContent.getFile().getDuration() > 0) {
            String time = messageContent.getFile().getDuration() + "s";
            holder.timeLength.setText(time);
        }

        String filePath = messageContent.getFile().getFilePath();
        final ChatMessageVideoContent content = (ChatMessageVideoContent) messageContent;

        if (filePath != null && new File(filePath).exists()) {
            holder.size.setVisibility(View.GONE);
            holder.playBtn.setImageResource(R.drawable.video_download_btn_nor);
            holder.iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playVideo(message);
                }
            });
        } else {
            if (messageContent.getFile().getSize() > 0) {
                String size = (int) messageContent.getFile().getSize() / 1024 + "kb";
                holder.size.setText("待下载" + size);
                holder.iv.setClickable(true);
                holder.iv.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Utils.showShortToast(activity, "播放视频");
                        content.download(new EngineCallback<String>() {
                            @Override
                            public void onSuccess(String s) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        holder.playBtn.setImageResource(R.drawable.video_download_btn_nor);
                                        holder.tv.setVisibility(View.GONE);
                                        holder.size.setVisibility(View.GONE);
                                        holder.iv.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                playVideo(message);
                                            }
                                        });
                                    }
                                });
                            }

                            @Override
                            public void onError(ResponseError error) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Utils.showShortToast(activity, "eroor");
                                    }
                                });
                            }
                        }, new ProgressCallback() {
                            @Override
                            public void progress(final int percent) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (percent != 100) {
                                            holder.tv.setVisibility(View.VISIBLE);
                                            holder.tv.setText(percent + "%");

                                        } else {
                                            holder.tv.setVisibility(View.GONE);
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
        handleMediaMessageStatus(holder, message);
    }

    private void playVideo(ChatMessage message) {
        Intent intent = new Intent(activity, ChatVideoPlayerActivity_.class);
        intent.putExtra(ChatVideoPlayerActivity.LOCALPATH, message.getContent().getFile().getFilePath());
        intent.putExtra(ChatVideoPlayerActivity.REMOTEPATH, message.getContent().getFile().getUrl());
        intent.putExtra(ChatVideoPlayerActivity.CHATMESSAGE, message.serialize());
        activity.startActivity(intent);
    }

    /**
     * 语音消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleVoiceMessage(final ChatMessage message,
                                    final ViewHolder holder, final int position, View convertView) {
        if (message.getContent().getFile().getDuration() != 0) {
            holder.tv.setText(formateDuration(message.getContent().getFile().getDuration() ) + "\"");
            holder.tv.setVisibility(View.VISIBLE);
        }

        VoicePlayClickListener voicePlayClickListener = new VoicePlayClickListener(activity, message, holder.iv, this, new ProgressCallback() {
            @Override
            public void progress(final int percent) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (percent != 100) {
                            holder.tv.setVisibility(View.VISIBLE);
                            holder.tv.setText(percent + "%");

                        } else {
                            if (message.getContent().getFile().getDuration() != 0) {
                                holder.tv.setText(formateDuration(message.getContent().getFile().getDuration() ) + "\"");
                                holder.tv.setVisibility(View.VISIBLE);
                            }else{
                                holder.tv.setVisibility(View.GONE);
                            }
                        }
                    }
                });
            }
        });
        holder.iv.setOnClickListener(voicePlayClickListener);
        handleMediaMessageStatus(holder, message);
    }


    /**
     * 处理位置消息
     *
     * @param message
     * @param holder
     * @param position
     * @param convertView
     */
    private void handleLocationMessage(final ChatMessage message,
                                       final ViewHolder holder, final int position, View convertView) {
        ChatMessageContent content = message.getContent();
        String poi = content.getLocation().getPoi();
        poi = poi == null || TextUtils.isEmpty(poi) ? "未知位置" : poi;
        holder.tv.setText(poi);
        if (!isNotByMe(message) && holder.pb != null) {
            holder.pb.setVisibility(View.GONE);
            return;
        }

        int status = MessageStatus.SUCCESS.getValue();

        if (message.getContent().getExtra() != null && message.getContent().getExtra().has("status")) {
            status = message.getContent().getExtra().get("status").getAsInt();
        }

        // deal with send message
        switch (MessageStatus.getMessageStatus(status)) {
            case SUCCESS:
                if (holder.pb != null) {
                    holder.pb.setVisibility(View.GONE);
                }
                if (holder.staus_iv != null) {
                    holder.staus_iv.setVisibility(View.GONE);
                }
                break;
            case FAIL:
                if (holder.pb != null) {
                    holder.pb.setVisibility(View.GONE);
                }
                if (holder.staus_iv != null) {
                    holder.staus_iv.setVisibility(View.VISIBLE);
                }
                break;
            case INPROGRESS:
                if (holder.pb != null) {
                    holder.pb.setVisibility(View.VISIBLE);
                }
                break;
            default:
                sendMsgInBackground(message, holder);
        }
    }

    /**
     * 发送消息
     *
     * @param message
     * @param holder
     */
    public void sendMsgInBackground(final ChatMessage message,
                                    final ViewHolder holder) {
        holder.staus_iv.setVisibility(View.GONE);
        holder.pb.setVisibility(View.VISIBLE);

    }

    /*
     * chat sdk will automatic download thumbnail image for the image message we
     * need to register callback show the download progress
     */
    private void showDownloadImageProgress(final ChatMessage message,
                                           final ViewHolder holder) {
        ChatMessageImageContent messageImageContent = (ChatMessageImageContent) message.getContent();
        messageImageContent.downloadThumb(new EngineCallback<String>() {
            @Override
            public void onSuccess(final String filePath) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        holder.pb.setVisibility(View.GONE);
                        holder.tv.setVisibility(View.GONE);
                        Bitmap bmp = BitmapFactory.decodeFile(filePath);
                        holder.iv.setImageBitmap(bmp);
                    }
                });
            }

            @Override
            public void onError(ResponseError error) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        holder.pb.setVisibility(View.GONE);
                        holder.tv.setVisibility(View.GONE);
                        holder.staus_iv.setVisibility(View.VISIBLE);
                        Utils.showShortToast(activity, "加载失败");
                    }
                });
            }
        }, new ProgressCallback() {
            @Override
            public void progress(final int percent) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        holder.tv.setText(percent + "%");
                    }
                });

            }
        });

    }


    /**
     * 更新ui上消息发送状态
     *
     * @param message
     * @param holder
     */
    private void updateSendedView(final ChatMessage message,
                                  final ViewHolder holder) {
    }

    /**
     * load image into image view
     *
     * @param iv
     * @return the image exists or not
     */
    private boolean showImageView(final ImageView iv, final ChatMessage message, boolean isThumber) {
        if (iv == null) {
            return false;
        }

        String filePath;
        String remoteUrl;
        if (isThumber) {
            filePath = message.getContent().getFile().getThumbnailPath();
            remoteUrl = message.getContent().getFile().getThumbUrl();

        } else {
            filePath = message.getContent().getFile().getFilePath();
            remoteUrl = message.getContent().getFile().getUrl();
        }

        LogUtils.i("file path:", filePath + "");
        LogUtils.i("remoteUrl:", remoteUrl + "");
        if (filePath != null && new File(filePath).exists()) {
            NetClient.getGirlBitmap(iv, "file:///" + filePath);
        } else if (remoteUrl != null) {
            NetClient.getGirlBitmap(iv, remoteUrl);
        } else {
            iv.setImageResource(R.drawable.default_image);
        }

        return false;
    }

    /**
     * 展示视频缩略图
     *
     * @param localThumb   本地缩略图路径
     * @param iv
     * @param thumbnailUrl 远程缩略图路径
     * @param message
     */
    private void showVideoThumbView(String localThumb, final ImageView iv,
                                    String thumbnailUrl, final ChatMessage message) {
        Bitmap bitmap = ImageCache.getInstance().get(localThumb);
        if (bitmap != null) {
            // thumbnail image is already loaded, reuse the drawable
            iv.setImageBitmap(bitmap);
        } else {
            final ChatMessageVideoContent content = (ChatMessageVideoContent) message.getContent();
            content.downloadThumb(new EngineCallback<String>() {
                @Override
                public void onSuccess(final String path) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bmp = BitmapFactory.decodeFile(path);
                            iv.setImageBitmap(bmp);
                            iv.setVisibility(View.VISIBLE);
                        }
                    });
                }

                @Override
                public void onError(ResponseError error) {

                }
            }, null);
        }
    }

    public void refresh(final List<ChatMessage> mMessageList) {
        if (mMessageList == null) {
            return;
        }
        this.setChatMessages(mMessageList);
        refresh();
    }

    public static class ViewHolder {
        ImageView iv;
        TextView tv;
        ProgressBar pb;
        ImageView staus_iv;
        ImageView head_iv;
        TextView tv_userId;
        ImageView playBtn;
        TextView timeLength;
        TextView size;
        LinearLayout container_status_btn;
        LinearLayout ll_container;
        ImageView iv_read_status;
    }
}