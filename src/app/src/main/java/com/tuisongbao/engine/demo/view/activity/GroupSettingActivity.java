package com.tuisongbao.engine.demo.view.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.chat.conversation.ChatConversation;
import com.tuisongbao.engine.chat.group.ChatGroup;
import com.tuisongbao.engine.chat.group.ChatGroupUser;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.demo.App;
import com.tuisongbao.engine.demo.GloableParams;
import com.tuisongbao.engine.demo.MainActivity_;
import com.tuisongbao.engine.demo.R;
import com.tuisongbao.engine.demo.bean.DemoGroup;
import com.tuisongbao.engine.demo.chat.ChatConversationActivity;
import com.tuisongbao.engine.demo.chat.adapter.GroupUserAdapter;
import com.tuisongbao.engine.demo.common.Utils;
import com.tuisongbao.engine.demo.view.BaseActivity;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsMenuItem;
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

    @ViewById(R.id.setting)
    LinearLayout settingLinearLayout;

    @OptionsMenuItem(R.id.menu_group_remove)
    MenuItem menuRemove;

    @OptionsMenuItem(R.id.menu_group_cancel)
    MenuItem menuCancel;

    @OptionsMenuItem(R.id.menu_group_add)
    MenuItem menuAdd;


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
    public void doSomethingAfterExtrasInjection() {
        mConversation = App.getInstance2().getConversationManager().loadOne(conversationTarget, conversationType);
        App.getInstance2().getGroupManager().getList(conversationTarget, new EngineCallback<List<ChatGroup>>() {
            @Override
            public void onSuccess(List<ChatGroup> chatGroups) {
                if (chatGroups != null && !chatGroups.isEmpty()) {
                    mGroup = chatGroups.get(0);
                    mDemoGroup = GloableParams.GroupInfos.get(mGroup.getGroupId());
                    if (mDemoGroup != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                groupDescription.setText(mDemoGroup.getDescription());
                                groupName.setText(mDemoGroup.getName());
                            }
                        });

                    }

                    if (App.getInstance2().getChatUser().getUserId().equals(mGroup.getOwner())){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(menuAdd != null && menuRemove != null){
                                    menuAdd.setVisible(true);
                                    menuRemove.setVisible(true);
                                }
                                settingLinearLayout.setVisibility(View.VISIBLE);
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
//        mConversation = App.getContext().getConversationManager().loadOne(conversationTarget,conversationType);
    }

    @AfterViews
    public void afterViews() {
        if (userIds == null) {
            userIds = new ArrayList<>();
        }

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

    void addUserToGroup() {
        // You can specify the ID in the annotation, or use the naming convention
        Intent intent = new Intent(this, AddUserToGroup_.class);
        ArrayList<String> usernames = new ArrayList(userIds);
        intent.putStringArrayListExtra("oldDemoUserNames", usernames);
        intent.putExtra(AddUserToGroup.EXTRA_GROUP, mGroup.serialize());
        startActivity(intent);
    }

    void removeUserInGroup() {
        groupUserAdapter.setIsEdit(true);
        menuCancel.setVisible(true);
        menuRemove.setVisible(false);
    }

    void cancelRemoveUserToGroup() {
        menuCancel.setVisible(false);
        menuRemove.setVisible(true);
        groupUserAdapter.setIsEdit(false);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home://返回上一菜单页
                Utils.finish(GroupSettingActivity.this);
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
