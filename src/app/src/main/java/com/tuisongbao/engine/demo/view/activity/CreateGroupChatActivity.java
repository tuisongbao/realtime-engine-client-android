package com.tuisongbao.engine.demo.view.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.apkfuns.logutils.LogUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.tuisongbao.engine.chat.group.ChatGroup;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.Constants;
import com.tuisongbao.engine.demo.GlobeParams;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.bean.DemoGroup;
import com.tuisongbao.engine.demo.common.Utils;
import com.tuisongbao.engine.demo.view.BaseActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.ViewById;
import org.apache.http.Header;

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


    @ViewById(R.id.cb_can_invite)
    CheckBox canInviteCheckBox;

    @ViewById(R.id.cb_is_public)
    CheckBox isPublicCheckBox;
    
    @AfterViews
    void afterView(){
        img_back.setVisibility(View.VISIBLE);
    }

    @Click(R.id.btn_create_group)
    void createGroup(){
        LogUtils.d(createBtn.isEnabled());
        createBtn.setEnabled(false);
        final String groupName = etGroupName.getText().toString();
        final String groupDescription = etGroupDescription.getText().toString();
        final Activity activity = this;
        try{
            boolean canInvite = canInviteCheckBox.isChecked();
            boolean isPublic = isPublicCheckBox.isChecked();

            App.getInstance().getGroupManager().create(null, isPublic, canInvite, new EngineCallback<ChatGroup>() {
                @Override
                public void onSuccess(final ChatGroup chatGroup) {
                    final RequestParams params = new RequestParams();
                    params.put("groupId", chatGroup.getGroupId());
                    params.put("name", groupName);
                    params.put("description", groupDescription);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            netClient.post(Constants.CREATEGROUPURL, params, new AsyncHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    DemoGroup demoGroup = new DemoGroup();
                                    demoGroup.setGroupId(chatGroup.getGroupId());
                                    demoGroup.setName(groupName);
                                    demoGroup.setDescription(groupDescription);
                                    GlobeParams.GroupInfo.put(chatGroup.getGroupId(),demoGroup);
                                    Intent intent = new Intent();
                                    intent.setClass(activity, GroupListActivity_.class);
                                    activity.startActivity(intent);
                                    activity.overridePendingTransition(R.anim.push_right_in,
                                            R.anim.push_right_out);
                                    finish();
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                    Utils.showShortToast(activity, "网络错误, 请重试");
                                    LogUtils.e(new String(responseBody));
                                    createBtn.setEnabled(true);
                                }
                            }, true);
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
