package com.tuisongbao.engine.demo.adpter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.bean.DemoGroup;
import com.tuisongbao.engine.demo.chat.ChatConversationActivity;
import com.tuisongbao.engine.demo.chat.ChatConversationActivity_;
import com.tuisongbao.engine.demo.common.ViewHolder;

import java.util.List;

/**
 * Created by user on 15-9-1.
 */

public class MyGroupAdpter extends BaseAdapter {
    protected Context context;
    private List<DemoGroup> grouplist;

    public MyGroupAdpter(Context ctx, List<DemoGroup> grouplist) {
        context = ctx;
        this.grouplist = grouplist;
    }

    @Override
    public int getCount() {
        return grouplist.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.layout_item_mygroup, parent, false);
        }
        final DemoGroup group = grouplist.get(position);
        ImageView img_avar = ViewHolder.get(convertView, R.id.img_photo);
        TextView txt_name = ViewHolder.get(convertView, R.id.txt_name);
        img_avar.setImageResource(R.drawable.defult_group);
        txt_name.setText(group.getName());
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,
                        ChatConversationActivity_.class);
                intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET, group.getGroupId());
                intent.putExtra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE, ChatType.GroupChat);
                context.startActivity(intent);
            }
        });
        return convertView;
    }

    public void refresh(List<DemoGroup> groupInfos) {

    }

}
