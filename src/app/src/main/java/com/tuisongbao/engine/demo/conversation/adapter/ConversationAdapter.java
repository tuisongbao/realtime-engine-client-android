package com.tuisongbao.engine.demo.conversation.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.chat.conversation.ChatConversation;
import com.tuisongbao.engine.chat.message.ChatMessage;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.GlobalParams;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.group.entity.DemoGroup;
import com.tuisongbao.engine.demo.common.utils.Utils;
import com.tuisongbao.engine.demo.common.utils.ViewHolder;
import com.tuisongbao.engine.demo.common.view.widght.dialog.WarnTipDialog;
import com.tuisongbao.engine.demo.common.utils.NetClient;
import com.tuisongbao.engine.demo.common.view.widght.swipe.SwipeLayout;

import java.util.Hashtable;
import java.util.List;

/**
 * Created by user on 15-9-1.
 */
public class ConversationAdapter extends BaseAdapter {
    protected Context context;
    private List<ChatConversation> conversationList;
    private WarnTipDialog Tipdialog;
    private int deleteID;
    private String target;
    private Hashtable<String, String> ChatRecord = new Hashtable<String, String>();

    public ConversationAdapter(Context ctx, List<ChatConversation> objects) {
        context = ctx;
        conversationList = objects;
    }

    @Override
    public int getCount() {
        return conversationList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.layout_item_msg, parent, false);
        }
        ImageView img_avar = ViewHolder.get(convertView,
                R.id.contactitem_avatar_iv);
        TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
        TextView txt_del = ViewHolder.get(convertView, R.id.txt_del);
        TextView txt_content = ViewHolder.get(convertView, R.id.txt_content);
        TextView txt_time = ViewHolder.get(convertView, R.id.txt_time);
        TextView unreadLabel = ViewHolder.get(convertView,
                R.id.unread_msg_number);
        SwipeLayout swipe = ViewHolder.get(convertView, R.id.swipe);
        
    
        swipe.setSwipeEnabled(true);
        // 获取与此用户/群组的会话
        final ChatConversation conversation = conversationList.get(position);
        
        // 获取用户username或者群组groupid
        target = conversation.getTarget();
        txt_del.setTag(target);

        NetClient.showAvatar(img_avar, target);
        if (conversation.getType().equals(ChatType.GroupChat)) {
            DemoGroup info = GlobalParams.GroupInfo.get(target);
            if (info != null) {
                txt_name.setText(info.getName());
            } else {
                txt_name.setText("未命名群组");
                // initGroupInfo(img_avar, txt_name);// 获取群组信息
            }
        } else {
            txt_name.setText(conversation.getTarget());
        }
        if (conversation.getUnreadMessageCount() > 0) {
            // 显示与此用户的消息未读数
            unreadLabel.setText(String.valueOf(conversation
                    .getUnreadMessageCount()));
            unreadLabel.setVisibility(View.VISIBLE);
        } else {
            unreadLabel.setVisibility(View.INVISIBLE);
        }
        if (conversation.getLastMessage() != null) {
            // 把最后一条消息的内容作为item的message内容
            ChatMessage lastMessage = conversation.getLastMessage();
            txt_content.setText(getMessageDigest(lastMessage, context));
            String time = Utils.formatDateTime(lastMessage.getCreatedAtInDate());
            txt_time.setText(time);
        }

        txt_del.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                deleteID = position;
                Tipdialog = new WarnTipDialog((Activity) context,
                        "您确定要删除该聊天吗？");
                Tipdialog.setBtnOkLinstener(onclick);
                Tipdialog.show();
            }
        });
        
        return convertView;
    }

    private DialogInterface.OnClickListener onclick = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            ChatConversation conversation = conversationList.get(deleteID);
            conversation.delete(new EngineCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            conversationList.remove(deleteID);
                            notifyDataSetChanged();
                            Tipdialog.dismiss();
                        }
                    });
                }

                @Override
                public void onError(ResponseError error) {

                }
            });
        }
    };

    /**
     * 根据消息内容和消息类型获取消息内容提示
     *
     * @param message
     * @param context
     * @return
     */
    private String getMessageDigest(ChatMessage message, Context context) {
        String digest = "";
        switch (message.getContent().getType()) {
            case LOCATION: // 位置消息
                digest = getString(context, R.string.location_message);
                break;
            case IMAGE: // 图片消息
                digest = getString(context, R.string.picture);
                break;
            case VOICE:// 语音消息
                digest = getString(context, R.string.voice_msg);
                break;
            case VIDEO: // 视频消息
                digest = getString(context, R.string.video);
                break;
            case EVENT: // 事件消息
                digest = getString(context, R.string.event);
                break;
            case TEXT: // 文本消息
                digest = message.getContent().getText();
                break;
            default:
                System.err.println("error, unknow type");
                return "";
        }
        return digest;
    }

    String getString(Context context, int resId) {
        return context.getResources().getString(resId);
    }

    public void setConversationList(List<ChatConversation> conversationList) {
        this.conversationList = conversationList;
    }

    public void refresh(List<ChatConversation> listConversation) {
        this.conversationList = listConversation;
        if (this.conversationList == null && this.conversationList.isEmpty()) {
            return;
        }

        notifyDataSetChanged();
    }
}
