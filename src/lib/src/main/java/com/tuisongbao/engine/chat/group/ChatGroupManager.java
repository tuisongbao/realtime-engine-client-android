package com.tuisongbao.engine.chat.group;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.engine.chat.group.entity.ChatGroup;
import com.tuisongbao.engine.chat.group.entity.ChatGroupCreateData;
import com.tuisongbao.engine.chat.group.entity.ChatGroupGetData;
import com.tuisongbao.engine.chat.group.entity.ChatGroupGetUsersData;
import com.tuisongbao.engine.chat.group.entity.ChatGroupJoinInvitationData;
import com.tuisongbao.engine.chat.group.entity.ChatGroupLeaveData;
import com.tuisongbao.engine.chat.group.entity.ChatGroupRemoveUserData;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.chat.group.event.ChatGroupCreateEvent;
import com.tuisongbao.engine.chat.group.event.handler.ChatGroupCreateEventHandler;
import com.tuisongbao.engine.chat.group.event.ChatGroupGetEvent;
import com.tuisongbao.engine.chat.group.event.handler.ChatGroupGetEventHandler;
import com.tuisongbao.engine.chat.group.event.ChatGroupGetUsersEvent;
import com.tuisongbao.engine.chat.group.event.handler.ChatGroupGetUsersEventHandler;
import com.tuisongbao.engine.chat.group.event.ChatGroupJoinInvitationEvent;
import com.tuisongbao.engine.chat.group.event.ChatGroupLeaveEvent;
import com.tuisongbao.engine.chat.group.event.ChatGroupRemoveUserEvent;
import com.tuisongbao.engine.common.BaseManager;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.event.handler.TSBCommonEventHandler;
import com.tuisongbao.engine.common.TSBEngineConstants;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.StrUtil;

import java.util.List;

public class ChatGroupManager extends BaseManager {
    private TSBGroupDataSource dataSource;
    private ChatManager mChatManager;

    public ChatGroupManager(TSBEngine engine) {
        mChatManager = engine.chatManager;
        if (mChatManager.isCacheEnabled()) {
            dataSource = new TSBGroupDataSource(TSBEngine.getContext(), engine);
        }
    }

    /**
     * 创建群组
     *
     * @param members
     *            群聊成员，可以为空，此时成员只有自己
     * @param callback
     */
    public void create(List<String> members,
            TSBEngineCallback<ChatGroup> callback) {
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
            TSBEngineCallback<ChatGroup> callback) {
        try {
            if (!mChatManager.isLogin()) {
                handleErrorMessage(callback,
                        TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                        "permission denny: need to login");
                return;
            }

            ChatGroupCreateEvent message = new ChatGroupCreateEvent();
            ChatGroupCreateData data = new ChatGroupCreateData();
            data.setInviteUserIds(members);
            data.setPublic(isPublic);
            data.setUserCanInvite(userCanInvite);
            message.setData(data);
            ChatGroupCreateEventHandler response = new ChatGroupCreateEventHandler();
            response.setCallback(callback);
            send(message, response);

        } catch (Exception e) {
            handleErrorMessage(callback, Protocol.ENGINE_CODE_UNKNOWN, Protocol.ENGINE_MESSAGE_UNKNOWN_ERROR);
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }
    }

    public void getList(String groupId, TSBEngineCallback<List<ChatGroup>> callback) {
        try {
            if (!mChatManager.isLogin()) {
                handleErrorMessage(callback,
                        TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                        "permission denny: need to login");
                return;
            }

            String lastActiveAt = null;
            if (mChatManager.isCacheEnabled()) {
                String userId = mChatManager.getChatUser().getUserId();
                dataSource.open();
                lastActiveAt = dataSource.getLatestLastActiveAt(userId);
                dataSource.close();
            }

            ChatGroupGetEvent message = new ChatGroupGetEvent();
            ChatGroupGetData data = new ChatGroupGetData();
            data.setGroupId(groupId);
            data.setLastActiveAt(lastActiveAt);
            message.setData(data);
            ChatGroupGetEventHandler response = new ChatGroupGetEventHandler();
            response.setCallback(callback);
            send(message, response);

        } catch (Exception e) {
            handleErrorMessage(callback, Protocol.ENGINE_CODE_UNKNOWN, Protocol.ENGINE_MESSAGE_UNKNOWN_ERROR);
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
            TSBEngineCallback<List<ChatUser>> callback) {
        try {
            if (!mChatManager.isLogin()) {
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
            ChatGroupGetUsersEvent message = new ChatGroupGetUsersEvent();
            ChatGroupGetUsersData data = new ChatGroupGetUsersData();
            data.setGroupId(groupId);
            message.setData(data);
            ChatGroupGetUsersEventHandler response = new ChatGroupGetUsersEventHandler();
            response.setCallback(callback);
            send(message, response);

        } catch (Exception e) {
            handleErrorMessage(callback, Protocol.ENGINE_CODE_UNKNOWN, Protocol.ENGINE_MESSAGE_UNKNOWN_ERROR);
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
            if (!mChatManager.isLogin()) {
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

            ChatGroupJoinInvitationEvent message = new ChatGroupJoinInvitationEvent();
            ChatGroupJoinInvitationData data = new ChatGroupJoinInvitationData();
            data.setGroupId(groupId);
            data.setUserIds(userIds);
            message.setData(data);
            TSBCommonEventHandler response = new TSBCommonEventHandler();
            response.setCallback(callback);
            send(message, response);

        } catch (Exception e) {
            handleErrorMessage(callback, Protocol.ENGINE_CODE_UNKNOWN, Protocol.ENGINE_MESSAGE_UNKNOWN_ERROR);
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
            if (!mChatManager.isLogin()) {
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

            ChatGroupRemoveUserEvent message = new ChatGroupRemoveUserEvent();
            ChatGroupRemoveUserData data = new ChatGroupRemoveUserData();
            data.setGroupId(groupId);
            data.setUserIds(userIds);
            message.setData(data);
            TSBCommonEventHandler response = new TSBCommonEventHandler();
            response.setCallback(callback);
            send(message, response);

        } catch (Exception e) {
            handleErrorMessage(callback, Protocol.ENGINE_CODE_UNKNOWN, Protocol.ENGINE_MESSAGE_UNKNOWN_ERROR);
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
            if (!mChatManager.isLogin()) {
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

            ChatGroupLeaveEvent message = new ChatGroupLeaveEvent();
            ChatGroupLeaveData data = new ChatGroupLeaveData();
            data.setGroupId(groupId);
            message.setData(data);
            TSBCommonEventHandler response = new TSBCommonEventHandler();
            response.setCallback(callback);
            send(message, response);
        } catch (Exception e) {
            handleErrorMessage(callback, Protocol.ENGINE_CODE_UNKNOWN, Protocol.ENGINE_MESSAGE_UNKNOWN_ERROR);
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
