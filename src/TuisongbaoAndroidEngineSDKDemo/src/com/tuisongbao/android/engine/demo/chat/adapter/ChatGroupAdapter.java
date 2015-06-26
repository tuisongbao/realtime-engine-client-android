package com.tuisongbao.android.engine.demo.chat.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.demo.R;

public class ChatGroupAdapter extends BaseAdapter {

    private Context mContext;
    private List<TSBChatGroup> mListGroup;

    public ChatGroupAdapter(List<TSBChatGroup> listGroup, Context context) {
        mListGroup = listGroup;
        mContext = context;
    }

    public void refresh(List<TSBChatGroup> listGroup) {
        mListGroup = listGroup;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mListGroup == null ? 0 : mListGroup.size();
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
                    R.layout.list_item_chat_group, null);
        }
        TSBChatGroup group = mListGroup.get(position);
        TextView textViewGroupId = (TextView) convertView
                .findViewById(R.id.list_item_chat_group_id);
        textViewGroupId.setText("groupId：" + group.getGroupId());
        TextView textViewOwner = (TextView) convertView
                .findViewById(R.id.list_item_chat_group_owner);
        textViewOwner.setText("Owner：" + group.getOwner());
        TextView textViewIspublic = (TextView) convertView
                .findViewById(R.id.list_item_chat_group_ispublic);
        textViewIspublic.setText("是否公开：" + group.isPublic());
        TextView textViewUserCanInvite = (TextView) convertView
                .findViewById(R.id.list_item_chat_group_usercaninvite);
        textViewUserCanInvite.setText("用户是否可以邀请：" + group.userCanInvite());
        TextView textViewUserCount = (TextView) convertView
                .findViewById(R.id.list_item_chat_group_user_count);
        textViewUserCount.setText("用户数：" + group.getUserCount());
        TextView textViewUserCountLimit = (TextView) convertView
                .findViewById(R.id.list_item_chat_group_user_count_limit);
        textViewUserCountLimit.setText("最大用户数：" + group.getUserCountLimit());

        return convertView;
    }
}
