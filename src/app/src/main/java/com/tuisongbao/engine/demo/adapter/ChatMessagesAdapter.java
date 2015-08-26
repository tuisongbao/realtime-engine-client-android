package com.tuisongbao.engine.demo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tuisongbao.engine.chat.message.ChatMessage;
import com.tuisongbao.engine.chat.message.ChatMessage.TYPE;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.app.App;
import com.tuisongbao.engine.demo.utils.AppUtil;

import java.util.List;

/**
 * Created by user on 15-8-24.
 */
public class ChatMessagesAdapter extends BaseAdapter {

    private static final String TAG = ChatMessagesAdapter.class.getName();

    public static final int TYPE_TEXT = 0;// 7种不同的布局
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VOICE = 2;
    public static final int TYPE_VIDEO = 3;
    public static final int TYPE_EVENT = 4;
    public static final int TYPE_LOCATION = 5;
    public static final int TYPE_UNKNOWN = 6;

    private LayoutInflater mInflater;

    private List<ChatMessage> myList;

    public ChatMessagesAdapter(Context context, List<ChatMessage> myList) {
        this.myList = myList;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return myList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return myList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    /**
     * 根据数据源的position返回需要显示的的layout的type
     *
     * type的值必须从0开始
     *
     * */
    @Override
    public int getItemViewType(int position) {

        ChatMessage msg = myList.get(position);
        TYPE type = msg.getContent().getType();

        int typeNum;

        if (TYPE.TEXT.equals(type)) {
            typeNum = TYPE_TEXT;
        }else if(TYPE.IMAGE.equals(type)){
            typeNum = TYPE_IMAGE;
        }else if(TYPE.VOICE.equals(type)){
            typeNum = TYPE_VOICE;
        }else if (TYPE.VIDEO.equals(type)){
            typeNum = TYPE_VIDEO;
        }else if (TYPE.EVENT.equals(type)){
            typeNum = TYPE_EVENT;
        }else if (TYPE.EVENT.LOCATION.equals(type)){
            typeNum = TYPE_LOCATION;
        }else {
            typeNum = TYPE_UNKNOWN;
        }

        Log.i(TAG, "TYPE : " + type);
        return typeNum;
    }

    /**
     * 返回所有的layout的数量
     *
     * */
    @Override
    public int getViewTypeCount() {
        return 7;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {

        final ChatMessage msg = myList.get(position);
        int type = getItemViewType(position);

        BaseMessageHolder baseMessageHolder;
        String username = msg.getFrom();
        if (convertView == null) {
            Log.i(TAG, "convertView : null" + type);
            switch (type) {
                case TYPE_TEXT:
                    baseMessageHolder = new TextMessageHolder();
                    TextMessageHolder textMessageHolder = (TextMessageHolder)baseMessageHolder;
                    if (App.getContext().getChatManager().getChatUser().getUserId().equals(username)){
                        convertView = mInflater.inflate(R.layout.cgt_layout_chat_msg_text_right, null);
                    }else {
                        convertView = mInflater.inflate(R.layout.cgt_layout_chat_msg_text_left, null);
                    }
                    textMessageHolder.tvText = (TextView)convertView.findViewById(R.id.cgt_tv_chat_msg_text);
                    textMessageHolder.tvText.setText(msg.getContent().getText());
                    break;
                case TYPE_IMAGE:
                    baseMessageHolder = new ImageMessageHolder();
                    final ImageMessageHolder imageMessageHolder = (ImageMessageHolder)baseMessageHolder;
                    if (App.getContext().getChatManager().getChatUser().getUserId().equals(username)){
                        convertView = mInflater.inflate(R.layout.demo_layout_chat_image_right, null);
                    }else {
                        convertView = mInflater.inflate(R.layout.demo_layout_chat_msg_image_left, null);
                    }
                    imageMessageHolder.ivImage = (ImageView)convertView.findViewById(R.id.iv_image);
                    imageMessageHolder.ivImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (v.getWidth() < msg.getContent().getFile().getWidth()){
                                ImageLoader.getInstance().displayImage(msg.getContent().getFile().getUrl(), imageMessageHolder.ivImage);
                            }else{
                                ImageLoader.getInstance().displayImage(msg.getContent().getFile().getThumbUrl(), imageMessageHolder.ivImage);
                            }
                        }
                    });
                    ImageLoader.getInstance().displayImage(msg.getContent().getFile().getThumbUrl(), imageMessageHolder.ivImage);
                    break;
                case TYPE_EVENT:
                    convertView = mInflater.inflate(R.layout.demo_layout_chat_event, null);
                    EventMessageHolder eventMessageHolder = new EventMessageHolder();
                    baseMessageHolder = eventMessageHolder;
                    eventMessageHolder.tvInfo = (TextView)convertView.findViewById(R.id.cgt_tv_chat_msg_text);
                    eventMessageHolder.tvInfo.setText(AppUtil.getEventMessage(msg));
                    break;
                default:
                    if (App.getContext().getChatManager().getChatUser().getUserId().equals(username)){
                        convertView = mInflater.inflate(R.layout.cgt_layout_chat_msg_text_right, null);
                    }else {
                        convertView = mInflater.inflate(R.layout.cgt_layout_chat_msg_text_left, null);
                    }
                    baseMessageHolder = new UnknownMessageHolder();
                    UnknownMessageHolder unknownMessageHolder = (UnknownMessageHolder)baseMessageHolder;
                    unknownMessageHolder.tvInfo = (TextView)convertView.findViewById(R.id.cgt_tv_chat_msg_text);
                    unknownMessageHolder.tvInfo.setText(msg.getContent().getType().getName());
                    break;
            }

            baseMessageHolder.tvTimeTip = (TextView)convertView.findViewById(R.id.cgt_tv_chat_msg_time);
            baseMessageHolder.avatarView = (ImageView)convertView.findViewById(R.id.cgt_iv_chat_userImg);
            baseMessageHolder.tvUsername = (TextView)convertView.findViewById(R.id.mTv_msg_username);

            convertView.setTag(baseMessageHolder);
        } else {
            Log.i(TAG, "convertView : not null");
            baseMessageHolder = (BaseMessageHolder)convertView.getTag();
            switch (type) {
                case TYPE_TEXT:
                    TextMessageHolder textMessageHolder = (TextMessageHolder)baseMessageHolder;
                    textMessageHolder.tvText.setText(msg.getContent().getText());
                    break;
                case TYPE_IMAGE:
//                    ImageMessageHolder imageMessageHolder = (ImageMessageHolder)baseMessageHolder;
//                    ImageLoader.getInstance().displayImage(msg.getContent().getFile().getThumbUrl(), imageMessageHolder.ivImage);
                    break;
                case TYPE_EVENT:
                    EventMessageHolder eventMessageHolder = (EventMessageHolder)(baseMessageHolder);
                    eventMessageHolder.tvInfo.setText(AppUtil.getEventMessage(msg));
                    break;
                default:
                    UnknownMessageHolder unknownMessageHolder = (UnknownMessageHolder)baseMessageHolder;
                    unknownMessageHolder.tvInfo.setText(msg.getContent().getType().getName());
                    break;
            }
        }

        if(TYPE_EVENT != type){
            ImageLoader.getInstance().displayImage(Constants.USERAVATARURL + username, baseMessageHolder.avatarView);
            baseMessageHolder.tvTimeTip.setText(msg.getCreatedAt());
            baseMessageHolder.tvUsername.setText(username);
        }
        return convertView;
    }

    public void refresh(List<ChatMessage> chatMessageList) {
        myList = chatMessageList;
        if (myList == null && myList.isEmpty()) {
            return;
        }

        notifyDataSetChanged();
    }

    class BaseMessageHolder {
        TextView tvTimeTip;
        ImageView avatarView;
        TextView tvUsername;
    }

    class TextMessageHolder extends BaseMessageHolder{
        TextView tvText;
    }

    class ImageMessageHolder extends BaseMessageHolder{
        ImageView ivImage;
    }

    class VoiceMessageHolder extends BaseMessageHolder {
        TextView tvInfo;// 声音时间
    }

    class VideoMessageHolder extends BaseMessageHolder{
        TextView tvInfo;
    }

    class LocationMessageHolder extends BaseMessageHolder{
        TextView tvInfo;
    }

    class EventMessageHolder extends BaseMessageHolder{
        TextView tvInfo;
    }

    class UnknownMessageHolder extends BaseMessageHolder{
        TextView tvInfo;
    }
}
