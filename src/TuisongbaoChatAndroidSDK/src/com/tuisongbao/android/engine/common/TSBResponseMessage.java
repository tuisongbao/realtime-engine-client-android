package com.tuisongbao.android.engine.common;

import java.util.List;

import android.content.Context;

import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.db.TSBConversationDataSource;
import com.tuisongbao.android.engine.chat.db.TSBGroupDataSource;
import com.tuisongbao.android.engine.chat.entity.TSBChatConversationData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupJoinInvitationData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupLeaveData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupRemoveUserData;
import com.tuisongbao.android.engine.chat.entity.TSBChatUser;
import com.tuisongbao.android.engine.chat.message.TSBChatConversationDeleteMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatConversationResetUnreadMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupJoinInvitationMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupLeaveMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupRemoveUserMessage;
import com.tuisongbao.android.engine.util.StrUtil;

/**
 * It's used to not need response data request.
 *
 */
public class TSBResponseMessage extends BaseTSBResponseMessage<String> {

    @Override
    protected void preCallBack(String data) {
        super.preCallBack(data);

        if (!TSBChatManager.getInstance().isUseCache()) {
            return;
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
        // 根据消息的名字进行相关处理
        if (StrUtil.isEqual(requestName, TSBChatGroupJoinInvitationMessage.NAME)) {
            TSBChatGroupJoinInvitationData joinInvitationData = (TSBChatGroupJoinInvitationData)requestData;
            String groupId = joinInvitationData.getGroupId();
            List<String> userIds = joinInvitationData.getUserIds();

            for (String userId : userIds) {
                groupDataSource.addUser(groupId, userId);
            }

        } else if (StrUtil.isEqual(requestName, TSBChatGroupLeaveMessage.NAME)) {
            TSBChatGroupLeaveData leaveData = (TSBChatGroupLeaveData)requestData;
            if (currentUser == null) {
                return;
            }
            groupDataSource.removeUser(leaveData.getGroupId(), currentUser.getUserId());

        } else if (StrUtil.isEqual(requestName, TSBChatGroupRemoveUserMessage.NAME)) {
            TSBChatGroupRemoveUserData removeUserData = (TSBChatGroupRemoveUserData)requestData;
            String groupId = removeUserData.getGroupId();
            List<String> userIds = removeUserData.getUserIds();

            for (String userId : userIds) {
                groupDataSource.removeUser(groupId, userId);
            }

        } else if (StrUtil.isEqual(requestName, TSBChatConversationDeleteMessage.NAME)) {
            TSBChatConversationData conversationData = (TSBChatConversationData)requestData;
            conversationDataSource.remove(currentUser.getUserId(), conversationData.getType(), conversationData.getTarget());

        } else if (StrUtil.isEqual(requestName, TSBChatConversationResetUnreadMessage.NAME)) {
            TSBChatConversationData conversationData = (TSBChatConversationData)requestData;
            conversationDataSource.resetUnread(currentUser.getUserId(), conversationData.getType(), conversationData.getTarget());
        }

        groupDataSource.close();
        conversationDataSource.close();
    }

    @Override
    public String parse() {
        return getData();
    }

}
