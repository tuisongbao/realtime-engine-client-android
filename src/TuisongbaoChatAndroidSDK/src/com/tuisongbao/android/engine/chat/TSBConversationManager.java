package com.tuisongbao.android.engine.chat;

import java.util.List;

import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.db.TSBConversationDataSource;
import com.tuisongbao.android.engine.chat.entity.ChatType;
import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.chat.entity.TSBChatConversationData;
import com.tuisongbao.android.engine.chat.entity.TSBChatMessageGetData;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatConversationDeleteMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatConversationGetMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatConversationGetReponseMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatConversationResetUnreadMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatMessageGetMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatMessageGetResponseMessage;
import com.tuisongbao.android.engine.common.BaseManager;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.common.TSBResponseMessage;
import com.tuisongbao.android.engine.entity.TSBEngineConstants;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBConversationManager extends BaseManager {
    private static TSBConversationManager mInstance;
    private static TSBConversationDataSource dataSource;

    public TSBConversationManager() {
        dataSource = new TSBConversationDataSource(TSBEngine.getContext());
    }

    public synchronized static TSBConversationManager getInstance() {
        if (mInstance == null) {
            mInstance = new TSBConversationManager();
        }
        return mInstance;
    }

    /**
     * 获取会话
     *
     * @param chatType
     *            可选， singleChat（单聊） 或 groupChat （群聊）
     * @param target
     *            可选，跟谁， userId 或 groupId
     * @param callback
     */
    public void getList(ChatType chatType, String target,
            TSBEngineCallback<List<TSBChatConversation>> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }

        dataSource.open();
        String lastActiveAt = dataSource.getLatestLastActiveAt();
        dataSource.close();

        TSBChatConversationGetMessage message = new TSBChatConversationGetMessage();
        TSBChatConversationData data = new TSBChatConversationData();
        data.setType(chatType);
        data.setTarget(target);
        // Only query the changes after this time.
        data.setLastActiveAt(lastActiveAt);
        message.setData(data);
        TSBChatConversationGetReponseMessage response = new TSBChatConversationGetReponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    /**
     * 重置未读消息
     *
     * @param chatType
     *            singleChat（单聊） 或 groupChat （群聊）
     * @param target
     *            跟谁， userId 或 groupId
     */
    public void resetUnread(ChatType chatType, String target, TSBEngineCallback<String> callback) {
        if (!isLogin()) {
            return;
        }
        if (chatType == null || StrUtil.isEmpty(target)) {
            return;
        }
        TSBChatConversationResetUnreadMessage message = new TSBChatConversationResetUnreadMessage();
        TSBChatConversationData data = new TSBChatConversationData();
        data.setType(chatType);
        data.setTarget(target);
        message.setData(data);
        TSBResponseMessage response = new TSBResponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    /**
     * 删除会话
     *
     * @param chatType
     *            singleChat（单聊） 或 groupChat （群聊）
     * @param target
     *            跟谁， userId 或 groupId
     * @param callback
     */
    public void delete(ChatType chatType, String target,
            TSBEngineCallback<String> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }
        if (chatType == null || StrUtil.isEmpty(target)) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: type or target can't not be empty");
            return;
        }
        TSBChatConversationDeleteMessage message = new TSBChatConversationDeleteMessage();
        TSBChatConversationData data = new TSBChatConversationData();
        data.setType(chatType);
        data.setTarget(target);
        message.setData(data);
        TSBResponseMessage response = new TSBResponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    /**
     * 获取消息
     *
     * @param chatType
     *            singleChat（单聊） 或 groupChat （群聊）
     * @param target
     *            跟谁， userId 或 groupId
     * @param startMessageId
     *            可选
     * @param endMessageId
     *            可选
     * @param limit
     *            可选，默认 20，最大 100
     */
    public void getMessages(ChatType chatType, String target, long startMessageId,
            long endMessageId, int limit,
            TSBEngineCallback<List<TSBMessage>> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }
        if (chatType == null) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: chat type ocan't not be empty");
            return;
        }
        if (StrUtil.isEmpty(target)) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: recipiet id can't not be empty");
            return;
        }

        dataSource.open();
        List<TSBMessage> messages = dataSource.getMessages(chatType, target, startMessageId, endMessageId);
        dataSource.close();

        if (messages.size() < 1) {
            sendRequestOfGetMessages(chatType, target, startMessageId, endMessageId, limit, callback);
            return;
        }

        long minCachedMessageId = messages.get(0).getMessageId();
        if (minCachedMessageId > startMessageId) {
            sendRequestOfGetMessages(chatType, target, startMessageId, minCachedMessageId, limit, callback);
        }
        long maxCachedMessageId = messages.get(messages.size() - 1).getMessageId();
        if (maxCachedMessageId < endMessageId) {
            sendRequestOfGetMessages(chatType, target, maxCachedMessageId, endMessageId, limit, callback);
        }

        long pre = minCachedMessageId;
        for (int i = 1; i < messages.size(); i++) {
            long next = messages.get(i).getMessageId();
            boolean needSendRequest = (next - minCachedMessageId) > 1;
            if (needSendRequest) {
                sendRequestOfGetMessages(chatType, target, pre, next, limit, callback);
            }
        }
    }

    private void sendRequestOfGetMessages(ChatType chatType, String target, long startMessageId,
            long endMessageId, int limit,
            TSBEngineCallback<List<TSBMessage>> callback) {

        TSBChatMessageGetMessage message = new TSBChatMessageGetMessage();
        TSBChatMessageGetData data = new TSBChatMessageGetData();
        data.setType(chatType);
        data.setTarget(target);
        data.setStartMessageId(startMessageId <= 0 ? null : startMessageId);
        data.setEndMessageId(endMessageId <= 0 ? null : endMessageId);
        data.setLimit(limit);
        message.setData(data);

        TSBChatMessageGetResponseMessage response = new TSBChatMessageGetResponseMessage();
        response.setCallback(callback);
        send(message, response);
    }
}
