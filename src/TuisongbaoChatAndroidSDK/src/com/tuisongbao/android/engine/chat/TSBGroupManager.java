package com.tuisongbao.android.engine.chat;

import java.util.List;

import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupCreateData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupGetData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupGetUsersData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupJoinInvitationData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupLeaveData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupRemoveUserData;
import com.tuisongbao.android.engine.chat.entity.TSBContactsUser;
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
import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.entity.TSBEngineConstants;
import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBGroupManager extends BaseManager {
    private static TSBGroupManager mInstance;
    private static TSBGroupDataSource dataSource;

    public TSBGroupManager() {
        if (TSBChatManager.getInstance().isCacheEnabled()) {
            dataSource = new TSBGroupDataSource(TSBEngine.getContext());
        }
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
     * @param members
     *            群聊成员，可以为空，此时成员只有自己
     * @param callback
     */
    public void create(List<String> members,
            TSBEngineCallback<TSBChatGroup> callback) {
        create(members, false, false, callback);
    }


    /**
     * 创建群组
     *
     * @param members
     *            群聊成员，可以为空，此时成员只有自己
     * @param isPublic
     *            默认值 true ，任何用户的加群请求都会直接通过，无需审核
     * @param userCanInvite
     *            默认值 true ，除创建者（owner）外，其他群用户也可以发送加群邀请
     * @param callback
     */
    public void create(List<String> members, boolean isPublic, boolean userCanInvite,
            TSBEngineCallback<TSBChatGroup> callback) {
        try {
            if (!isLogin()) {
                handleErrorMessage(callback,
                        TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                        "permission denny: need to login");
                return;
            }

            TSBChatGroupCreateMessage message = new TSBChatGroupCreateMessage();
            TSBChatGroupCreateData data = new TSBChatGroupCreateData();
            data.setInviteUserIds(members);
            data.setPublic(isPublic);
            data.setUserCanInvite(userCanInvite);
            message.setData(data);
            TSBChatGroupCreateReponseMessage response = new TSBChatGroupCreateReponseMessage();
            response.setCallback(callback);
            send(message, response);

        } catch (Exception e) {
            handleErrorMessage(callback, EngineConstants.ENGINE_CODE_UNKNOWN, EngineConstants.ENGINE_MESSAGE_UNKNOWN_ERROR);
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }
    }

    /**
     * 获取群组列表
     *
     * @param tsbChatManager TODO
     * @param groupId
     *            可选，根据 id 过滤
     * @param callback
     */
    public void getList(String groupId, TSBEngineCallback<List<TSBChatGroup>> callback) {
        try {
            if (!isLogin()) {
                handleErrorMessage(callback,
                        TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                        "permission denny: need to login");
                return;
            }

            String lastActiveAt = null;
            if (TSBChatManager.getInstance().isCacheEnabled()) {
                String userId = TSBChatManager.getInstance().getChatUser().getUserId();
                dataSource.open();
                lastActiveAt = dataSource.getLatestLastActiveAt(userId);
                dataSource.close();
            }

            TSBChatGroupGetMessage message = new TSBChatGroupGetMessage();
            TSBChatGroupGetData data = new TSBChatGroupGetData();
            data.setGroupId(groupId);
            data.setLastActiveAt(lastActiveAt);
            message.setData(data);
            TSBChatGroupGetReponseMessage response = new TSBChatGroupGetReponseMessage();
            response.setCallback(callback);
            send(message, response);

        } catch (Exception e) {
            handleErrorMessage(callback, EngineConstants.ENGINE_CODE_UNKNOWN, EngineConstants.ENGINE_MESSAGE_UNKNOWN_ERROR);
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }

    }


    /**
     * 获取群组下用户列表，会从服务器同步最新的数据
     *
     * @param groupId
     * @param callback
     */
    public void getUsers(String groupId,
            TSBEngineCallback<List<TSBContactsUser>> callback) {
        try {
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

        } catch (Exception e) {
            handleErrorMessage(callback, EngineConstants.ENGINE_CODE_UNKNOWN, EngineConstants.ENGINE_MESSAGE_UNKNOWN_ERROR);
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }
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
        try {
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

        } catch (Exception e) {
            handleErrorMessage(callback, EngineConstants.ENGINE_CODE_UNKNOWN, EngineConstants.ENGINE_MESSAGE_UNKNOWN_ERROR);
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }
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
        try {
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

        } catch (Exception e) {
            handleErrorMessage(callback, EngineConstants.ENGINE_CODE_UNKNOWN, EngineConstants.ENGINE_MESSAGE_UNKNOWN_ERROR);
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }
    }

    /**
     * 离开群组
     *
     * @param groupId
     *            群的id
     * @param callback
     */
    public void leave(String groupId, TSBEngineCallback<String> callback) {
        try {
            if (!isLogin()) {
                handleErrorMessage(callback,
                        TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                        "permission denny: need to login");
                return;
            }
            if (StrUtil.isEmpty(groupId)) {
                handleErrorMessage(callback,
                        TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                        "illegal parameter: group id can not be empty");
                return;
            }

            TSBChatGroupLeaveMessage message = new TSBChatGroupLeaveMessage();
            TSBChatGroupLeaveData data = new TSBChatGroupLeaveData();
            data.setGroupId(groupId);
            message.setData(data);
            TSBResponseMessage response = new TSBResponseMessage();
            response.setCallback(callback);
            send(message, response);
        } catch (Exception e) {
            handleErrorMessage(callback, EngineConstants.ENGINE_CODE_UNKNOWN, EngineConstants.ENGINE_MESSAGE_UNKNOWN_ERROR);
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }
    }

    /***
     * Remove all Groups and related users from local database.
     */
    public void clearCache() {
        dataSource.deleteAllData();
    }
}
