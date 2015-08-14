package com.tuisongbao.engine.chat.group;

import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.db.ChatGroupDataSource;
import com.tuisongbao.engine.chat.group.entity.ChatGroup;
import com.tuisongbao.engine.chat.group.entity.ChatGroupEventData;
import com.tuisongbao.engine.chat.group.event.ChatGroupCreateEvent;
import com.tuisongbao.engine.chat.group.event.ChatGroupGetEvent;
import com.tuisongbao.engine.chat.group.event.ChatGroupGetUsersEvent;
import com.tuisongbao.engine.chat.group.event.ChatGroupJoinInvitationEvent;
import com.tuisongbao.engine.chat.group.event.ChatGroupLeaveEvent;
import com.tuisongbao.engine.chat.group.event.ChatGroupRemoveUsersEvent;
import com.tuisongbao.engine.chat.group.event.handler.ChatGroupCreateEventHandler;
import com.tuisongbao.engine.chat.group.event.handler.ChatGroupGetEventHandler;
import com.tuisongbao.engine.chat.group.event.handler.ChatGroupGetUsersEventHandler;
import com.tuisongbao.engine.chat.group.event.handler.ChatGroupJoinInvitationEventHandler;
import com.tuisongbao.engine.chat.group.event.handler.ChatGroupLeaveEventHandler;
import com.tuisongbao.engine.chat.group.event.handler.ChatGroupRemoveUsersEventHandler;
import com.tuisongbao.engine.chat.user.entity.ChatUserPresence;
import com.tuisongbao.engine.common.BaseManager;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.log.LogUtil;

import java.util.List;

/**
 * {@link ChatGroup} 的管理类，每个 {@link Engine} 只有一个该实例。开启缓存时，
 * 所有的API调用会根据缓存数据适当从服务器获取最新的数据，减少流量。
 *
 * @see ChatManager#enableCache()
 */
public final class ChatGroupManager extends BaseManager {
    private static final String TAG = "TSB" + ChatGroupManager.class.getSimpleName();

    private ChatGroupDataSource dataSource;
    private final ChatManager mChatManager;

    public ChatGroupManager(Engine engine) {
        mChatManager = engine.getChatManager();
        if (mChatManager.isCacheEnabled()) {
            dataSource = new ChatGroupDataSource(Engine.getContext(), engine);
        }
    }

    public void create(List<String> members,
            EngineCallback<ChatGroup> callback) {
        create(members, false, false, callback);
    }

    /**
     * 创建群组
     *
     * @param members 成员列表，由 UserId 作为每个成员的标识
     * @param isPublic {@code true}
     * @param userCanInvite {@code true} 表示成员可以邀请新成员加入，否则只有创建者可以
     * @param callback 结果通知函数
     */
    public void create(List<String> members, boolean isPublic, boolean userCanInvite,
            EngineCallback<ChatGroup> callback) {
        try {
            ChatGroupCreateEvent event = new ChatGroupCreateEvent();
            ChatGroupEventData data = new ChatGroupEventData();
            data.setInviteUserIds(members);
            data.setPublic(isPublic);
            data.setUserCanInvite(userCanInvite);
            event.setData(data);
            ChatGroupCreateEventHandler response = new ChatGroupCreateEventHandler();
            response.setCallback(callback);
            send(event, response);
        } catch (Exception e) {
            callback.onError(engine.getUnhandledResponseError());
            LogUtil.error(TAG, e);
        }
    }

    /**
     * 获取当前用户的群组列表。
     *
     * @param groupId 必填
     * @param callback 结果通知函数
     */
    public void getList(String groupId, EngineCallback<List<ChatGroup>> callback) {
        try {
            if (!mChatManager.hasLogin()) {
                ResponseError error = new ResponseError();
                error.setMessage("Permission denny: need to login");
                callback.onError(error);
                return;
            }

            String lastActiveAt = null;
            if (mChatManager.isCacheEnabled()) {
                String userId = mChatManager.getChatUser().getUserId();
                dataSource.open();
                lastActiveAt = dataSource.getLatestLastActiveAt(userId);
                dataSource.close();
            }

            ChatGroupGetEvent event = new ChatGroupGetEvent();
            ChatGroupEventData data = new ChatGroupEventData();
            data.setGroupId(groupId);
            data.setLastActiveAt(lastActiveAt);
            event.setData(data);
            ChatGroupGetEventHandler response = new ChatGroupGetEventHandler();
            response.setCallback(callback);
            send(event, response);

        } catch (Exception e) {
            callback.onError(engine.getUnhandledResponseError());
            LogUtil.error(TAG, e);
        }

    }


    /**
     * 获取群组下用户列表
     *
     * @param groupId 必填
     * @param callback 结果通知函数
     */
    public void getUsers(String groupId,
            EngineCallback<List<ChatUserPresence>> callback) {
        try {
            ChatGroupGetUsersEvent event = new ChatGroupGetUsersEvent();
            ChatGroupEventData data = new ChatGroupEventData();
            data.setGroupId(groupId);
            event.setData(data);
            ChatGroupGetUsersEventHandler response = new ChatGroupGetUsersEventHandler();
            response.setCallback(callback);
            send(event, response);

        } catch (Exception e) {
            callback.onError(engine.getUnhandledResponseError());
            LogUtil.error(TAG, e);
        }
    }

    /**
     * 邀请用户加入群组
     *
     * @param groupId 必填
     * @param userIds 邀请加入的用户id列表
     * @param callback 结果通知函数
     */
    public void joinInvitation(String groupId, List<String> userIds,
            EngineCallback<String> callback) {
        try {
            ChatGroupJoinInvitationEvent event = new ChatGroupJoinInvitationEvent();
            ChatGroupEventData data = new ChatGroupEventData();
            data.setGroupId(groupId);
            data.setUserIds(userIds);
            event.setData(data);
            ChatGroupJoinInvitationEventHandler response = new ChatGroupJoinInvitationEventHandler();
            response.setCallback(callback);
            send(event, response);

        } catch (Exception e) {
            callback.onError(engine.getUnhandledResponseError());
            LogUtil.error(TAG, e);
        }
    }

    /**
     * 移除群组中的用户
     *
     * @param groupId 必填
     * @param userIds 被移除的用户的id列表
     * @param callback 结果通知函数
     */
    public void removeUsers(String groupId, List<String> userIds,
            EngineCallback<String> callback) {
        try {
            ChatGroupRemoveUsersEvent event = new ChatGroupRemoveUsersEvent();
            ChatGroupEventData data = new ChatGroupEventData();
            data.setGroupId(groupId);
            data.setUserIds(userIds);
            event.setData(data);
            ChatGroupRemoveUsersEventHandler response = new ChatGroupRemoveUsersEventHandler();
            response.setCallback(callback);
            send(event, response);

        } catch (Exception e) {
            callback.onError(engine.getUnhandledResponseError());
            LogUtil.error(TAG, e);
        }
    }

    /**
     * 当前用户离开群组
     *
     * @param groupId 必填
     * @param callback 结果通知函数
     */
    public void leave(String groupId, EngineCallback<String> callback) {
        try {
            ChatGroupLeaveEvent event = new ChatGroupLeaveEvent();
            ChatGroupEventData data = new ChatGroupEventData();
            data.setGroupId(groupId);
            event.setData(data);
            ChatGroupLeaveEventHandler response = new ChatGroupLeaveEventHandler();
            response.setCallback(callback);
            send(event, response);
        } catch (Exception e) {
            callback.onError(engine.getUnhandledResponseError());
            LogUtil.error(TAG, e);
        }
    }

    public void clearCache() {
        dataSource.deleteAllData();
    }
}
