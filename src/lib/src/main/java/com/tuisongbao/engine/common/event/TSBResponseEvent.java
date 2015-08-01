package com.tuisongbao.engine.common.event;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.conversation.entity.ChatConversationData;
import com.tuisongbao.engine.chat.conversation.event.ChatConversationDeleteEvent;
import com.tuisongbao.engine.chat.conversation.event.ChatConversationResetUnreadEvent;
import com.tuisongbao.engine.chat.db.TSBConversationDataSource;
import com.tuisongbao.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.engine.chat.group.entity.ChatGroupJoinInvitationData;
import com.tuisongbao.engine.chat.group.entity.ChatGroupLeaveData;
import com.tuisongbao.engine.chat.group.entity.ChatGroupRemoveUserData;
import com.tuisongbao.engine.chat.group.event.ChatGroupJoinInvitationEvent;
import com.tuisongbao.engine.chat.group.event.ChatGroupLeaveEvent;
import com.tuisongbao.engine.chat.group.event.ChatGroupRemoveUserEvent;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.util.StrUtil;

import java.util.List;

/**
 * It's used to not need response data request.
 *
 */
public class TSBResponseEvent extends BaseResponseEvent<String> {

    @Override
    protected String prepareCallBackData() {
        String result = super.prepareCallBackData();

        if (!mEngine.chatManager.isCacheEnabled()) {
            return result;
        }

        // 初始化数据库
        Context applicationContext = TSBEngine.getContext();
        TSBGroupDataSource groupDataSource = new TSBGroupDataSource(applicationContext, mEngine.chatManager);
        TSBConversationDataSource conversationDataSource = new TSBConversationDataSource(applicationContext, mEngine.chatManager);

        groupDataSource.open();
        conversationDataSource.open();

        ChatUser currentUser = mEngine.chatManager.getChatUser();

        // 获取response的名称，和request时的名称是一致的
        String requestName = getBindName();

        // 获取request的传递参数（在TSBListenerSink的callbackListener方法中传入的）
        Object requestData = getRequestData();
        Gson gson = new Gson();

        // 根据消息的名字进行相关处理
        if (StrUtil.isEqual(requestName, ChatGroupJoinInvitationEvent.NAME)) {
            ChatGroupJoinInvitationData joinInvitationData = gson.fromJson((String)requestData, ChatGroupJoinInvitationData.class);
            String groupId = joinInvitationData.getGroupId();
            List<String> userIds = joinInvitationData.getUserIds();

            for (String userId : userIds) {
                groupDataSource.insertUserIfNotExist(groupId, userId);
            }

        } else if (StrUtil.isEqual(requestName, ChatGroupLeaveEvent.NAME)) {
            ChatGroupLeaveData leaveData = gson.fromJson((String)requestData, ChatGroupLeaveData.class);
            if (currentUser == null) {
                return result;
            }
            groupDataSource.removeUser(leaveData.getGroupId(), currentUser.getUserId());

        } else if (StrUtil.isEqual(requestName, ChatGroupRemoveUserEvent.NAME)) {
            ChatGroupRemoveUserData removeUserData = gson.fromJson((String)requestData, ChatGroupRemoveUserData.class);
            String groupId = removeUserData.getGroupId();

            for (String userId : removeUserData.getUserIds()) {
                groupDataSource.removeUser(groupId, userId);
            }

        } else if (StrUtil.isEqual(requestName, ChatConversationDeleteEvent.NAME)) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(ChatType.class, new TSBChatMessageChatTypeSerializer());
            ChatConversationData conversationData = gsonBuilder.create().fromJson((String)requestData,
                    new TypeToken<ChatConversationData>() {}.getType());
            conversationDataSource.remove(currentUser.getUserId(), conversationData.getType(), conversationData.getTarget());

        } else if (StrUtil.isEqual(requestName, ChatConversationResetUnreadEvent.NAME)) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(ChatType.class, new TSBChatMessageChatTypeSerializer());
            ChatConversationData conversationData = gsonBuilder.create().fromJson((String)requestData,
                    new TypeToken<ChatConversationData>() {}.getType());
            conversationDataSource.resetUnread(currentUser.getUserId(), conversationData.getType(), conversationData.getTarget());
        }

        groupDataSource.close();
        conversationDataSource.close();
        return result;
    }

    @Override
    public String parse() {
        return getData();
    }

}
