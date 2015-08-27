package com.tuisongbao.engine.demo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.entity.DemoUser;
import com.tuisongbao.engine.demo.utils.L;
import com.tuisongbao.engine.demo.utils.LogUtil;

import java.util.List;

/**
 * Created by user on 15-8-27.
 */
public class DemoUserAdapter extends BaseAdapter{
    private static final String TAG = LogUtil.makeLogTag(DemoUserAdapter.class);

    private Context mContext;

    private List<DemoUser> mList;

    public DemoUserAdapter(Context mContext, List<DemoUser> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.demo_user_item, null);
            holder.userAvatar = (ImageView) convertView.findViewById(R.id.avatar);
            holder.userName = (TextView) convertView.findViewById(R.id.userName);
            convertView.setTag(holder);
        }

        DemoUser demoUser = mList.get(position);
        ImageLoader.getInstance().displayImage(Constants.USERAVATARURL + demoUser.getUsername(), holder.userAvatar);
        holder.userName.setText(demoUser.getUsername());

        return convertView;
    }

    public void refresh(List<DemoUser> demoUsers) {
        mList = demoUsers;
        if (mList == null) {
            return;
        }
        L.i(TAG, "--------------list" + demoUsers);
        notifyDataSetChanged();
    }

    class ViewHolder {
        /**
         * 头像
         **/
        ImageView userAvatar;
        /**
         * 名称
         **/
        TextView userName;
    }
}
