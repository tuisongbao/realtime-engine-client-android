package com.tuisongbao.engine.demo.view.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.juns.health.net.loopj.android.http.JsonHttpResponseHandler;
import com.juns.health.net.loopj.android.http.RequestParams;
import com.tuisongbao.engine.chat.group.ChatGroup;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.GloableParams;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.bean.DemoGroup;
import com.tuisongbao.engine.demo.common.Utils;
import com.tuisongbao.engine.demo.view.BaseActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

/**
 * Created by user on 15-9-1.
 */
@EActivity(R.layout.activity_create_group)
public class CreateGroupChatActivity extends BaseActivity{
    @ViewById(R.id.img_back)
    ImageView img_back;
    @ViewById(R.id.et_group_name)
    EditText etGroupName;

    @ViewById(R.id.et_group_description)
    EditText etGroupDescription;

    @ViewById(R.id.btn_create_group)
    Button createBtn;
    
    @AfterViews
    void afterView(){
        img_back.setVisibility(View.VISIBLE);
    }

    @Click(R.id.btn_create_group)
    void createGroup(){
        createBtn.setEnabled(false);
        final String groupName = etGroupName.getText().toString();
        final String groupDescription = etGroupDescription.getText().toString();
        if(TextUtils.isEmpty(groupName) || TextUtils.isEmpty(groupDescription) ){
            Utils.showShortToast(this, "信息不能为空");
            createBtn.setEnabled(true);
            return;
        }
        final Activity activity = this;
        try{
            App.getInstance2().getGroupManager().create(null, new EngineCallback<ChatGroup>() {
                @Override
                public void onSuccess(final ChatGroup chatGroup) {
                    RequestParams params = new RequestParams();
                    params.put("groupId", chatGroup.getGroupId());
                    params.put("name", groupName);
                    params.put("description", groupDescription);
                    netClient.post(Constants.CREATEGROUPAPI, params, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            DemoGroup demoGroup = new DemoGroup();
                            demoGroup.setGroupId(chatGroup.getGroupId());
                            demoGroup.setName(groupName);
                            demoGroup.setDescription(groupDescription);
                            GloableParams.GroupInfos.put(chatGroup.getGroupId(),demoGroup);
                            Utils.start_Activity(activity, GroupListActivity_.class);
                        }

                        @Override
                        public void onFailure(final Throwable e, final JSONObject errorResponse) {
                            if(errorResponse == null){
                                onSuccess(new JSONObject());
                                return;
                            }
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("error", e.getMessage() + errorResponse + "");
                                    Utils.showShortToast(activity, "网络错误, 请重试");
                                    createBtn.setEnabled(true);
                                }
                            });
                        }
                    });
                }

                @Override
                public void onError(ResponseError error) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showShortToast(activity, "网络错误, 请重试");
                        }
                    });
                }
            });

        }catch (Exception e){
            Utils.showShortToast(this, "网络错误, 请重试");
            createBtn.setEnabled(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @TextChange({R.id.et_group_name, R.id.et_group_description})
    void textChange(){
        String groupName = etGroupName.getText().toString();
        String groupDescription = etGroupDescription.getText().toString();
        Log.i("register", groupName + groupDescription);
        if(TextUtils.isEmpty(groupName) || TextUtils.isEmpty(groupDescription) ){
            createBtn.setEnabled(true);
            return;
        }
        createBtn.setBackground(getResources().getDrawable(
                R.drawable.btn_bg_green));
        createBtn.setEnabled(true);
    }

    @Click(R.id.img_back)
    void back(){
        Utils.finish(CreateGroupChatActivity.this);
    }

}
