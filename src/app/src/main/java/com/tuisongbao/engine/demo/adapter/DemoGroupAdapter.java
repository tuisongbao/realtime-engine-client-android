package com.tuisongbao.engine.demo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.entity.DemoGroup;

import java.util.List;

/**
 * Created by user on 15-8-26.
 */
public class DemoGroupAdapter extends BaseAdapter{
    private List<DemoGroup> demoGroups;
    private Context mContext;


    public DemoGroupAdapter(List<DemoGroup> demoGroups, Context mContext) {
        if (demoGroups == null) {
            return;
        }
        this.demoGroups = demoGroups;
        this.mContext = mContext;
    }



    @Override
    public int getCount() {
        return demoGroups.size();
    }

    @Override
    public Object getItem(int position) {
        return demoGroups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = View.inflate(mContext, R.layout.group_item, null);
        TextView groupName = (TextView) convertView.findViewById(R.id.group_name);
        DemoGroup demoGroup =  demoGroups.get(position);
        String name = "";
        if(demoGroup != null){
            name =demoGroup.getName();
        }
        groupName.setText(name);
        return convertView;
    }

    public void refresh(List<DemoGroup> demoGroups) {
        this.demoGroups = demoGroups;
        if (demoGroups == null && demoGroups.isEmpty()) {
            return;
        }
        notifyDataSetChanged();
    }
}
