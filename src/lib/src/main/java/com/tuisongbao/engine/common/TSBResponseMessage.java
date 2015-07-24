package com.tuisongbao.engine.common;

import java.util.List;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.TSBChatManager;
import com.tuisongbao.engine.chat.db.TSBConversationDataSource;
import com.tuisongbao.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.engine.chat.entity.ChatType;
import com.tuisongbao.engine.chat.entity.TSBChatConversationData;
import com.tuisongbao.engine.chat.entity.TSBChatGroupJoinInvitationData;
import com.tuisongbao.engine.chat.entity.TSBChatGroupLeaveData;
import com.tuisongbao.engine.chat.entity.TSBChatGroupRemoveUserData;
import com.tuisongbao.engine.chat.entity.TSBChatUser;
import com.tuisongbao.engine.chat.message.TSBChatConversationDeleteMessage;
import com.tuisongbao.engine.chat.message.TSBChatConversationResetUnreadMessage;
import com.tuisongbao.engine.chat.message.TSBChatGroupJoinInvitationMessage;
import com.tuisongbao.engine.chat.message.TSBChatGroupLeaveMessage;
import com.tuisongbao.engine.chat.message.TSBChatGroupRemoveUserMessage;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.engine.util.StrUtil;

/**
 * It's used to not need response data request.
 *
 */
public class TSBResponseMessage extends BaseTSBResponseMessage<String> {

    @Override
    protected String prepareCallBackData() {
        String result = super.prepareCallBackData();

        if (!TSBChatManager.getInstance().isCacheEnabled()) {
            return result;
        }

        // 初始化数据库
        Context applicationContext = TSBEngine.getContext();
        TSBGroupDataSource groupDataSource = new TSBGroupDataSource(applicationContext);
        TSBConversationDataSource conversationDataSource = new TSBConversationDataSource(applicationContext);

        groupDataSource.open();
        conversationDataSource.open();

        TSBChatUser currentUser = TSBChatManager.getInstance().getChatUser();

        // 获取response的名称，和request时的名称是一致的
        String requestName = getBindName();

        // 获取request的传递参数（在TSBListenerSink的callbackListener方法中传入的）
        Object requestData = getRequestData();
        Gson gson = new Gson();

        // 根据消息的名字进行相关处理
        if (StrUtil.isEqual(requestName, TSBChatGroupJoinInvitationMessage.NAME)) {
            TSBChatGroupJoinInvitationData joinInvitationData = gson.fromJson((String)requestData, TSBChatGroupJoinInvitationData.class);
            String groupId = joinInvitationData.getGroupId();
            List<String> userIds = joinInvitationData.getUserIds();

            for (String userId : userIds) {
                groupDataSource.insertUserIfNotExist(groupId, userId);
            }

        } else if (StrUtil.isEqual(requestName, TSBChatGroupLeaveMessage.NAME)) {
            TSBChatGroupLeaveData leaveData = gson.fromJson((String)requestData, TSBChatGroupLeaveData.class);
            if (currentUser == null) {
                return result;
            }
            groupDataSource.removeUser(leaveData.getGroupId(), currentUser.getUserId());

        } else if (StrUtil.isEqual(requestName, TSBChatGroupRemoveUserMessage.NAME)) {
            TSBChatGroupRemoveUserData removeUserData = gson.fromJson((String)requestData, TSBChatGroupRemoveUserData.class);
            String groupId = removeUserData.getGroupId();

            for (String userId : removeUserData.getUserIds()) {
                groupDataSource.removeUser(groupId, userId);
            }

        } else if (StrUtil.isEqual(requestName, TSBChatConversationDeleteMessage.NAME)) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(ChatType.class, new TSBChatMessageChatTypeSerializer());
            TSBChatConversationData conversationData = gsonBuilder.create().fromJson((String)requestData,
                    new TypeToken<TSBChatConversationData>() {}.getType());
            conversationDataSource.remove(currentUser.getUserId(), conversationData.getType(), conversationData.getTarget());

        } else if (StrUtil.isEqual(requestName, TSBChatConversationResetUnreadMessage.NAME)) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(ChatType.class, new TSBChatMessageChatTypeSerializer());
            TSBChatConversationData conversationData = gsonBuilder.create().fromJson((String)requestData,
                    new TypeToken<TSBChatConversationData>() {}.getType());
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
