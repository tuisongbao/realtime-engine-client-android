package com.tuisongbao.engine.demo.group.view.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.chat.conversation.ChatConversation;
import com.tuisongbao.engine.chat.group.ChatGroup;
import com.tuisongbao.engine.chat.group.ChatGroupUser;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.GlobeParams;
import com.tuisongbao.engine.demo.MainActivity_;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.common.utils.Utils;
import com.tuisongbao.engine.demo.common.view.activity.BaseActivity;
import com.tuisongbao.engine.demo.conversation.view.activity.ChatConversationActivity;
import com.tuisongbao.engine.demo.user.adapter.GroupUserAdapter;
import com.tuisongbao.engine.demo.group.entity.DemoGroup;
import com.tuisongbao.engine.demo.user.view.activity.AddUserToGroupActivity_;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 15-9-2.
 */
@EActivity(R.layout.activity_group_info)
public class GroupSettingActivity extends BaseActivity {
    @Extra(ChatConversationActivity.EXTRA_CONVERSATION_TARGET)
    String conversationTarget;

    @Extra(ChatConversationActivity.EXTRA_CONVERSATION_TYPE)
    ChatType conversationType;

    @ViewById(R.id.user_list)
    GridView userList;

    @ViewById(R.id.iv_group_remove)
    ImageView ivRemove;

    @ViewById(R.id.iv_group_cancel)
    ImageView ivCancel;

    @ViewById(R.id.iv_group_add)
    ImageView ivAdd;

    @ViewById(R.id.img_back)
    ImageView img_back;

    @ViewById(R.id.cb_can_invite)
    CheckBox canInviteCheckBox;

    @ViewById(R.id.cb_is_public)
    CheckBox isPublicCheckBox;


    private ChatGroup mGroup;
    private DemoGroup mDemoGroup;
    private ChatConversation mConversation;
    private List<String> userIds;
    private GroupUserAdapter groupUserAdapter;

    @ViewById(R.id.group_description)
    TextView groupDescription;

    @ViewById(R.id.group_name)
    TextView groupName;

    @AfterExtras
    public void afterExtras() {
        mConversation = App.getInstance().getConversationManager().loadOne(conversationTarget, conversationType);
        App.getInstance().getGroupManager().getList(conversationTarget, new EngineCallback<List<ChatGroup>>() {
            @Override
            public void onSuccess(List<ChatGroup> chatGroups) {
                if (chatGroups != null && !chatGroups.isEmpty()) {
                    mGroup = chatGroups.get(0);
                    isPublicCheckBox.setChecked(mGroup.isPublic());
                    canInviteCheckBox.setChecked(mGroup.userCanInvite());
                    mDemoGroup = GlobeParams.GroupInfo.get(mGroup.getGroupId());
                    if (mDemoGroup != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                groupDescription.setText(mDemoGroup.getDescription());
                                groupName.setText(mDemoGroup.getName());
                            }
                        });

                    }else{
                        // TODO 尝试更新一次 demo group 的信息
                    }

                    if (App.getInstance().getChatUser().getUserId().equals(mGroup.getOwner())){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(ivAdd != null && ivRemove != null){
                                    ivAdd.setVisibility(View.VISIBLE);
                                    ivRemove.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                    request();
                }
            }

            @Override
            public void onError(ResponseError error) {

            }
        });
    }

    @AfterViews
    public void afterViews() {
        if (userIds == null) {
            userIds = new ArrayList<>();
        }

        img_back.setVisibility(View.VISIBLE);
        groupUserAdapter = new GroupUserAdapter(userIds, this);

        userList.setAdapter(groupUserAdapter);
    }

    public void request() {
        mGroup.getUsers(new EngineCallback<List<ChatGroupUser>>() {
            @Override
            public void onSuccess(List<ChatGroupUser> chatGroupUsers) {
                for (ChatGroupUser user : chatGroupUsers) {
                    userIds.add(user.getUserId());
                }

                runOnUiThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                    @Override
                    public void run() {
                        groupUserAdapter.refresh(userIds);
                    }
                });
            }

            @Override
            public void onError(ResponseError error) {

            }
        });
    }

    @Click(R.id.iv_group_add)
    void addUserToGroup() {
        // You can specify the ID in the annotation, or use the naming convention
        Intent intent = new Intent(this, AddUserToGroupActivity_.class);
        ArrayList<String> usernames = new ArrayList(userIds);
        intent.putStringArrayListExtra("oldDemoUserNames", usernames);
        intent.putExtra(AddUserToGroupActivity_.EXTRA_GROUP, mGroup.serialize());
        startActivity(intent);
    }

    @Click(R.id.iv_group_remove)
    void removeUserInGroup() {
        groupUserAdapter.setIsEdit(true);
        ivCancel.setVisibility(View.VISIBLE);
        ivRemove.setVisibility(View.GONE);
    }

    @Click(R.id.iv_group_cancel)
    void cancelRemoveUserToGroup() {
        ivCancel.setVisibility(View.GONE);
        ivRemove.setVisibility(View.VISIBLE);
        groupUserAdapter.setIsEdit(false);
    }

    @Click(R.id.img_back)
    void back() {
        Utils.finish(GroupSettingActivity.this);
    }

    @ItemClick(R.id.user_list)
    public void myListItemClicked(final String username) {
        if (!groupUserAdapter.isEdit()){
            return;
        }
        List<String> removeUserIds = new ArrayList<>();
        removeUserIds.add(username);
        mGroup.removeUsers(removeUserIds, new EngineCallback<String>() {
            @Override
            public void onSuccess(String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        userIds.remove(username);
                        groupUserAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(ResponseError error) {
            }
        });
    }

    @Click(R.id.delete_conversation)
    public void deleteConversation() {
        mConversation.delete(new EngineCallback<String>() {
            @Override
            public void onSuccess(String s) {
                Intent intent = new Intent(GroupSettingActivity.this, MainActivity_.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(ResponseError error) {

            }
        });
    }
}
