package com.tuisongbao.engine.demo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
import com.tuisongbao.engine.chat.message.entity.ChatMessageContent;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.GlobalParams;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.entity.DemoGroup;
import com.tuisongbao.engine.demo.utils.DemoGroupUtil;
import com.tuisongbao.engine.demo.utils.LogUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 15-8-19.
 */
@EBean(scope = EBean.Scope.Singleton)
public class ConversationAdapter extends BaseAdapter {
    private static final String TAG = LogUtil.makeLogTag(ConversationAdapter.class);

    private Context mContext;

    public void setmList(List<ChatConversation> mList) {
        this.mList = mList;
    }

    private List<ChatConversation> mList;

    @Bean
    DemoGroupUtil demoGroupUtil;

    public ConversationAdapter() {
        this.mContext = GlobalParams.activity;
        this.mList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        if (mList == null) {
            return 0;
        } else {
            return mList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.conversation_item, null);
            holder.conversationAvatar = (ImageView) convertView.findViewById(R.id.cgt_iv_userPhoto);
            holder.conversationName = (TextView) convertView.findViewById(R.id.cgt_tv_userName);
            holder.lastMessage = (TextView) convertView.findViewById(R.id.cgt_tv_personalizedSignature);
            holder.date = (TextView) convertView.findViewById(R.id.cgt_tv_date);
            convertView.setTag(holder);
        }

        String target = mList.get(position).getTarget();
        if ("singleChat".equals(mList.get(position).getType().getName())) {
            holder.conversationName.setText(mList.get(position).getTarget());
        } else {
            String name = mList.get(position).getTarget();
            Map<String, DemoGroup> groups = demoGroupUtil.getDemoGroups();
            if (groups.containsKey(target)) {
                name = groups.get(target).getName();
            }
            holder.conversationName.setText(name);
        }
        ImageLoader.getInstance().displayImage(Constants.USERAVATARURL + target, holder.conversationAvatar);
        ChatMessageContent content = mList.get(position).getLastMessage().getContent();
        String lastMessage = content.getText() != null ? content.getText() : content.getType().toString();
        holder.lastMessage.setText(lastMessage);
        holder.date.setText(mList.get(position).getLastActiveAt());

        return convertView;
    }

    public void refresh(List<ChatConversation> listConversation) {
        mList = listConversation;
        if (mList == null && mList.isEmpty()) {
            return;
        }

        notifyDataSetChanged();
        updateGroups();
    }

    @UiThread
    void updateGroupInfo() {
        notifyDataSetChanged();
    }

    @Background
    void updateGroups() {
        List<String> ids = new ArrayList<>();

        for (ChatConversation conversation : mList) {
            if (!"singleChat".equals(conversation.getType().getName())
                    && !demoGroupUtil.getDemoGroups().containsKey(conversation.getTarget())) {
                ids.add(conversation.getTarget());
            }
        }

        if (ids.size() < 0) {
            return;
        }

        demoGroupUtil.getDemoGroups(ids);
        updateGroupInfo();
    }

    class ViewHolder {
        /**
         * 头像
         **/
        ImageView conversationAvatar;
        /**
         * 名称
         **/
        TextView conversationName;
        /**
         * 最后一条信息
         **/
        TextView lastMessage;
        /**
         * 时间
         **/
        TextView date;
    }

}
