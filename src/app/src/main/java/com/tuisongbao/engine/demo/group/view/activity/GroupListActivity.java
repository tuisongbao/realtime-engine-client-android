package com.tuisongbao.engine.demo.group.view.activity;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tuisongbao.engine.chat.group.ChatGroup;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.GlobalParams;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.common.utils.Utils;
import com.tuisongbao.engine.demo.common.view.activity.BaseActivity;
import com.tuisongbao.engine.demo.group.adapter.DemoGroupAdapter;
import com.tuisongbao.engine.demo.group.entity.DemoGroup;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 15-9-1.
 */
@EActivity(R.layout.activity_listview)
public class GroupListActivity extends BaseActivity{
    @ViewById(R.id.txt_title)
    TextView txt_title;

    @ViewById(R.id.imgBack)
    ImageView img_back;

    @ViewById(R.id.imgRight)
    ImageView img_right;

    @ViewById(R.id.listview)
    ListView mlistview;

    List<DemoGroup> groupInfos;

    DemoGroupAdapter myGroupAdpter;

    @AfterViews
    void afterViews() {
        txt_title.setText("群聊");
        img_back.setVisibility(View.VISIBLE);
        img_right = (ImageView) findViewById(R.id.imgRight);
        img_right.setImageResource(R.drawable.icon_add);
        img_right.setVisibility(View.VISIBLE);
        mlistview = (ListView) findViewById(R.id.listview);
        View layout_head = getLayoutInflater().inflate(
                R.layout.layout_head_search, null);
        mlistview.addHeaderView(layout_head);
        refresh();
    }

    protected void initView() {
        if (groupInfos != null && groupInfos.size() > 0) {
            mlistview.setAdapter(new DemoGroupAdapter(this, groupInfos));
        } else {
            TextView txt_nodata = (TextView) findViewById(R.id.txt_nochat);
            txt_nodata.setText("暂时没有群聊");
            txt_nodata.setVisibility(View.VISIBLE);
        }
    }

    protected void refresh() {
        App.getInstance().getGroupManager().getList(null, new EngineCallback<List<ChatGroup>>() {
            @Override
            public void onSuccess(List<ChatGroup> chatGroups) {
                if(chatGroups == null || chatGroups.isEmpty()){
                    return;
                }
                groupInfos = new ArrayList<>();
                for (ChatGroup group :
                        chatGroups) {
                    DemoGroup demoGroup = GlobalParams.GroupInfo.get(group.getGroupId());

                    if (demoGroup == null) {
                        demoGroup = new DemoGroup(group.getGroupId(), "未命名群组", "");
                    }

                    groupInfos.add(demoGroup);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initView();
                    }
                });
            }

            @Override
            public void onError(ResponseError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initView();
                    }
                });
            }
        });
    }


    @Click(R.id.imgBack)
    void back(){
        Utils.finish(GroupListActivity.this);
    }

    @Click(R.id.imgRight)
    void gotoCreateGroupActivity(){
        Utils.start_Activity(GroupListActivity.this,
                CreateGroupChatActivity_.class);
    }

}