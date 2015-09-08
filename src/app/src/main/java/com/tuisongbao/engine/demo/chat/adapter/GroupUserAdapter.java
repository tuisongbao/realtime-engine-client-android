package com.tuisongbao.engine.demo.chat.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.readystatesoftware.viewbadger.BadgeView;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by user on 15-9-2.
 */
public class GroupUserAdapter extends BaseAdapter {
    private boolean isEdit;
    public GroupUserAdapter(List<String> userIds, Context mContext) {
        if (userIds == null) {
            return;
        }
        this.userIds = sortUserId(userIds);
        this.mContext = mContext;
        this.isEdit = false;
    }

    public GroupUserAdapter(List<String> userIds, Context mContext, boolean isEdit) {
        if (userIds == null) {
            return;
        }
        this.userIds = sortUserId(userIds);
        this.mContext = mContext;
        this.isEdit = isEdit;
    }

    private List<String> sortUserId(List<String> userIds) {
        Collections.sort(userIds, new Comparator<String>() {
            @Override
            public int compare(String userId1, String userId2) {
                if (userId1.equals(App.getInstance2().getChatUser().getUserId())) {
                    return -1;
                } else if (userId2.equals(App.getInstance2().getChatUser().getUserId())) {
                    return 1;
                } else {
                    return String.CASE_INSENSITIVE_ORDER.compare(userId1, userId2);

                }
            }
        });
        return userIds;
    }

    private List<String> userIds;
    private Context mContext;

    @Override
    public int getCount() {
        return userIds.size();
    }

    @Override
    public Object getItem(int position) {
        return userIds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = View.inflate(mContext, R.layout.conversation_user_item, null);
        ImageView avatar = (ImageView) convertView.findViewById(R.id.avatar);
        TextView username = (TextView) convertView.findViewById(R.id.userName);
        String name = userIds.get(position);
        username.setText(name);
        ImageLoader.getInstance().displayImage(Constants.USERAVATARURL + name, avatar);
        if(avatar != null && isEdit){
            BadgeView badge = new BadgeView(mContext, avatar);
            badge.setBadgePosition(BadgeView.POSITION_TOP_LEFT);
            badge.setText("X");
            badge.show();
        }

        return convertView;
    }

    public void refresh(List<String> ids) {
        userIds = ids;
        if (userIds == null && userIds.isEmpty()) {
            return;
        }
        this.userIds = sortUserId(userIds);
        notifyDataSetChanged();
    }

    public void setIsEdit(boolean isEdit) {
        this.isEdit = isEdit;
        notifyDataSetChanged();
    }

    public boolean isEdit() {
        return isEdit;
    }
}
