package com.tuisongbao.engine.chat;

import java.util.ArrayList;
import java.util.List;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.db.TSBConversationDataSource;
import com.tuisongbao.engine.chat.entity.ChatType;
import com.tuisongbao.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.engine.chat.entity.TSBChatConversationData;
import com.tuisongbao.engine.chat.entity.TSBChatMessageGetData;
import com.tuisongbao.engine.chat.entity.TSBMessage;
import com.tuisongbao.engine.chat.message.TSBChatConversationDeleteMessage;
import com.tuisongbao.engine.chat.message.TSBChatConversationGetMessage;
import com.tuisongbao.engine.chat.message.TSBChatConversationGetReponseMessage;
import com.tuisongbao.engine.chat.message.TSBChatConversationResetUnreadMessage;
import com.tuisongbao.engine.chat.message.TSBChatMessageGetMessage;
import com.tuisongbao.engine.chat.message.TSBChatMessageGetResponseMessage;
import com.tuisongbao.engine.chat.message.TSBChatMessageMultiGetResponseMessage;
import com.tuisongbao.engine.common.BaseManager;
import com.tuisongbao.engine.common.TSBEngineCallback;
import com.tuisongbao.engine.common.TSBResponseMessage;
import com.tuisongbao.engine.engineio.EngineConstants;
import com.tuisongbao.engine.entity.TSBEngineConstants;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.StrUtil;

public class TSBConversationManager extends BaseManager {
    private static TSBConversationManager mInstance;
    private static TSBConversationDataSource dataSource;

    public TSBConversationManager() {
        if (TSBChatManager.getInstance().isCacheEnabled()) {
            dataSource = new TSBConversationDataSource(TSBEngine.getContext());
        }
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
        try {
            if (!isLogin()) {
                handleErrorMessage(callback,
                        TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                        "permission denny: need to login");
                return;
            }

            String lastActiveAt = null;
            if (TSBChatManager.getInstance().isCacheEnabled()) {
                dataSource.open();
                String userId = TSBChatManager.getInstance().getChatUser().getUserId();
                lastActiveAt = dataSource.getLatestLastActiveAt(userId);
                dataSource.close();
            }
            sendRequestOfGetConversations(chatType, target, lastActiveAt, callback);
        } catch (Exception e) {
            handleErrorMessage(callback, EngineConstants.ENGINE_CODE_UNKNOWN, EngineConstants.ENGINE_MESSAGE_UNKNOWN_ERROR);
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }
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
        try {
            if (!isLogin()) {
                return;
            }
            if (chatType == null || StrUtil.isEmpty(target)) {
                return;
            }

            if (TSBChatManager.getInstance().isCacheEnabled()) {
                // Reset unread event has no response from server, so directly update database.
                String userId = TSBChatManager.getInstance().getChatUser().getUserId();
                dataSource.open();
                dataSource.resetUnread(userId, chatType, target);
                dataSource.close();
            }

            TSBChatConversationResetUnreadMessage message = new TSBChatConversationResetUnreadMessage();
            TSBChatConversationData data = new TSBChatConversationData();
            data.setType(chatType);
            data.setTarget(target);
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
        try {
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

        } catch (Exception e) {
            handleErrorMessage(callback, EngineConstants.ENGINE_CODE_UNKNOWN, EngineConstants.ENGINE_MESSAGE_UNKNOWN_ERROR);
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }
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
    public void getMessages(ChatType chatType, String target, Long startMessageId,
            Long endMessageId, int limit,
            TSBEngineCallback<List<TSBMessage>> callback) {
        try {
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

            // No need to query if startMessageId is less or equal to 0
            if (startMessageId != null && startMessageId <= 0) {
                callback.onSuccess(new ArrayList<TSBMessage>());
                return;
            }

            if (!TSBChatManager.getInstance().isCacheEnabled()) {
                TSBChatMessageGetMessage message = getRequestOfGetMessages(chatType, target, startMessageId, endMessageId, limit);
                TSBChatMessageGetResponseMessage response = new TSBChatMessageGetResponseMessage();
                response.setCallback(callback);
                send(message, response);
                return;
            }

            requestMissingMessagesInLocalCache(chatType, target, startMessageId, endMessageId, limit, callback);
        } catch (Exception e) {
            handleErrorMessage(callback, EngineConstants.ENGINE_CODE_UNKNOWN, EngineConstants.ENGINE_MESSAGE_UNKNOWN_ERROR);
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }
    }

    /***
     * Remove all conversations and related messages from local database.
     */
    public void clearCache() {
        dataSource.deleteAllData();
    }

    private void requestMissingMessagesInLocalCache(ChatType chatType, String target, Long startMessageId,
            Long endMessageId, int limit, TSBEngineCallback<List<TSBMessage>> callback) {
        TSBChatMessageMultiGetResponseMessage response = new TSBChatMessageMultiGetResponseMessage();
        response.setMessageIdSpan(startMessageId, endMessageId);
        response.setCallback(callback);

        String currentUserId = TSBChatManager.getInstance().getChatUser().getUserId();
        // Query local data
        dataSource.open();
        List<TSBMessage> messages = dataSource.getMessages(currentUserId, chatType, target, startMessageId, endMessageId, limit);
        LogUtil.debug(LogUtil.LOG_TAG_CHAT_CACHE, "Get " + messages.size() + " messages");
        dataSource.close();

        // if startMessageId is null, pull the latest messages.
        if (messages.size() < 1 || startMessageId == null) {
            TSBChatMessageGetMessage message = getRequestOfGetMessages(chatType, target, startMessageId, endMessageId, limit);
            response.incRequestCount();
            send(message, response);
            return;
        }

        // Check whether missing messages from begin.
        Long maxCachedMessageId = messages.get(0).getMessageId();
        if (startMessageId != null && maxCachedMessageId < startMessageId) {
            TSBChatMessageGetMessage message = getRequestOfGetMessages(chatType, target, startMessageId, maxCachedMessageId, limit);
            response.incRequestCount();
            send(message, response);
        }
        // Check whether missing messages from end.
        Long minCachedMessageId = messages.get(messages.size() - 1).getMessageId();
        if (endMessageId != null && minCachedMessageId > endMessageId) {
            TSBChatMessageGetMessage message = getRequestOfGetMessages(chatType, target, minCachedMessageId, endMessageId, limit);
            response.incRequestCount();
            send(message, response);
        }
        // Check missing messages between messages of local DB
        Long pre = maxCachedMessageId;
        for (int i = 1; i < messages.size(); i++) {
            Long next = messages.get(i).getMessageId();
            boolean needSendRequest = (pre - next) > 1;
            if (needSendRequest) {
                TSBChatMessageGetMessage message = getRequestOfGetMessages(chatType, target, pre, next, limit);
                response.incRequestCount();
                send(message, response);
            }
            pre = next;
        }
        // All request messages is in local DB
        if (response.getRequestCount() < 1) {
            callback.onSuccess(messages);
        }
    }

    private TSBChatMessageGetMessage getRequestOfGetMessages(ChatType chatType, String target, Long startMessageId,
            Long endMessageId, int limit) {
        TSBChatMessageGetMessage message = new TSBChatMessageGetMessage();
        TSBChatMessageGetData data = new TSBChatMessageGetData();
        data.setType(chatType);
        data.setTarget(target);
        data.setStartMessageId(startMessageId);
        data.setEndMessageId(endMessageId);
        data.setLimit(limit);
        message.setData(data);

        return message;
    }

    private void sendRequestOfGetConversations(ChatType chatType, String target, String lastActiveAt,
            TSBEngineCallback<List<TSBChatConversation>> callback) {
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
}
