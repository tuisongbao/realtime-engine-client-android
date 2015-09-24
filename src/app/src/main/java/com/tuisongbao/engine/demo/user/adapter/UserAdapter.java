package com.tuisongbao.engine.demo.user.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.tuisongbao.engine.demo.user.entity.DemoUser;
import com.tuisongbao.engine.demo.user.view.widght.ChoiceListItemView;

import java.util.List;

/**
 * Created by user on 15-9-22.
 */
public class UserAdapter extends BaseAdapter {

    private List<DemoUser> mList;
    private Context mContext;

    public UserAdapter(Context mContext, List<DemoUser> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.size();
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
        ChoiceListItemView choiceListItemView = new ChoiceListItemView(mContext, null);
        choiceListItemView.setName(mList.get(position).getUsername());

        return choiceListItemView;
    }

    public void refresh(List<DemoUser> demoUsers) {
        mList = demoUsers;
        if (mList == null) {
            return;
        }

        notifyDataSetChanged();
    }
}
