package com.tuisongbao.android.engine.chat.groups;

import java.util.List;

import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupCreateData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupGetData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupGetUsersData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupJoinInvitationData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupLeaveData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupRemoveUserData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupUser;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupCreateMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupCreateReponseMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupGetMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupGetReponseMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupGetUsersMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupGetUsersReponseMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupJoinInvitationMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupLeaveMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupRemoveUserMessage;
import com.tuisongbao.android.engine.common.BaseManager;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.common.TSBResponseMessage;
import com.tuisongbao.android.engine.entity.TSBEngineConstants;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBGroupManager extends BaseManager {
    private static TSBGroupManager mInstance;

    public TSBGroupManager() {
    }

    public synchronized static TSBGroupManager getInstance() {
        if (mInstance == null) {
            mInstance = new TSBGroupManager();
        }
        return mInstance;
    }

    /**
     * 创建群组
     *
     * @param groupName
     *            群的名称 注意, 群名称不能以 private- 或者 present- 开头
     * @param members
     *            群聊成员，可以为空，此时成员只有自己
     * @param callback
     */
    public void createGroup(String groupName, List<String> members,
            TSBEngineCallback<TSBChatGroup> callback) {
        createGroup(groupName, "", members, false, false, callback);
    }


    /**
     * 创建群组
     *
     * @param groupName
     *            群的名称 注意, 群名称不能以 private- 或者 present- 开头
     * @param description
     *            群的描述
     * @param members
     *            群聊成员，可以为空，此时成员只有自己
     * @param isPublic
     *            默认值 true ，任何用户的加群请求都会直接通过，无需审核
     * @param userCanInvite
     *            默认值 true ，除创建者（owner）外，其他群用户也可以发送加群邀请
     * @param callback
     */
    public void createGroup(String groupName, String description,
            List<String> members, boolean isPublic, boolean userCanInvite,
            TSBEngineCallback<TSBChatGroup> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }
        if (!isIllegalGroupName(groupName)) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: group name can't not be empty or illegal");
            return;
        }

        TSBChatGroupCreateMessage message = new TSBChatGroupCreateMessage();
        TSBChatGroupCreateData data = new TSBChatGroupCreateData();
        data.setName(groupName);
        data.setDescription(description);
        data.setInviteUserIds(members);
        data.setPublic(isPublic);
        data.setUserCanInvite(userCanInvite);
        message.setData(data);
        TSBChatGroupCreateReponseMessage response = new TSBChatGroupCreateReponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    /**
     * 获取群组列表
     *
     * @param tsbChatManager TODO
     * @param groupId
     *            可选，根据 id 过滤
     * @param groupName
     *            可选，根据 name 过滤
     * @param callback
     */
    public void getGroups(String groupId, String groupName, TSBEngineCallback<List<TSBChatGroup>> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }

        TSBChatGroupGetMessage message = new TSBChatGroupGetMessage();
        TSBChatGroupGetData data = new TSBChatGroupGetData();
        data.setGroupId(groupId);
        data.setName(groupName);
        message.setData(data);
        TSBChatGroupGetReponseMessage response = new TSBChatGroupGetReponseMessage();
        response.setCallback(callback);
        send(message, response);
    }


    /**
     * 获取群组下用户列表
     *
     * @param groupId
     * @param callback
     */
    public void getUsers(String groupId,
            TSBEngineCallback<List<TSBChatGroupUser>> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }
        if (StrUtil.isEmpty(groupId)) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: group id can't be not empty");
            return;
        }
        TSBChatGroupGetUsersMessage message = new TSBChatGroupGetUsersMessage();
        TSBChatGroupGetUsersData data = new TSBChatGroupGetUsersData();
        data.setGroupId(groupId);
        message.setData(data);
        TSBChatGroupGetUsersReponseMessage response = new TSBChatGroupGetUsersReponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    /**
     * 邀请加入群组
     *
     * @param groupId
     *            群的id
     * @param userIds
     *            邀请加入的用户id
     * @param callback
     */
    public void joinInvitation(String groupId, List<String> userIds,
            TSBEngineCallback<String> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }
        if (StrUtil.isEmpty(groupId) || userIds == null || userIds.isEmpty()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: group id or user ids can't not be empty");
            return;
        }

        TSBChatGroupJoinInvitationMessage message = new TSBChatGroupJoinInvitationMessage();
        TSBChatGroupJoinInvitationData data = new TSBChatGroupJoinInvitationData();
        data.setGroupId(groupId);
        data.setUserIds(userIds);
        message.setData(data);
        TSBResponseMessage response = new TSBResponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    /**
     * 删除群组中的用户
     *
     * @param groupId
     *            群的id
     * @param userIds
     *            删除的用户id
     * @param callback
     */
    public void removeUsers(String groupId, List<String> userIds,
            TSBEngineCallback<String> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }
        if (StrUtil.isEmpty(groupId) || userIds == null || userIds.isEmpty()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: group id or user ids can't not be empty");
            return;
        }

        TSBChatGroupRemoveUserMessage message = new TSBChatGroupRemoveUserMessage();
        TSBChatGroupRemoveUserData data = new TSBChatGroupRemoveUserData();
        data.setGroupId(groupId);
        data.setUserIds(userIds);
        message.setData(data);
        TSBResponseMessage response = new TSBResponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    /**
     * 离开群组
     *
     * @param groupId
     *            群的id
     * @param callback
     */
    public void leaveGroup(String groupId, TSBEngineCallback<String> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }
        if (StrUtil.isEmpty(groupId)) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: group id can't not be empty");
            return;
        }

        TSBChatGroupLeaveMessage message = new TSBChatGroupLeaveMessage();
        TSBChatGroupLeaveData data = new TSBChatGroupLeaveData();
        data.setGroupId(groupId);
        message.setData(data);
        TSBResponseMessage response = new TSBResponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    private boolean isIllegalGroupName(String groupName) {
        return !StrUtil.isEmpty(groupName)
                && !groupName
                        .startsWith(TSBEngineConstants.TSBENGINE_CHANNEL_PREFIX_PRIVATE)
                && !groupName
                        .startsWith(TSBEngineConstants.TSBENGINE_CHANNEL_PREFIX_PRESENCE);
    }

    private boolean isLogin() {
        return TSBChatManager.getInstance().isLogin();
    }
}
