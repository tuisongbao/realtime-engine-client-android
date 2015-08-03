package com.tuisongbao.engine.chat.conversation;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.ChatOptions;
import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
import com.tuisongbao.engine.chat.conversation.entity.ChatConversationData;
import com.tuisongbao.engine.chat.conversation.event.ChatConversationDeleteEvent;
import com.tuisongbao.engine.chat.conversation.event.ChatConversationGetEvent;
import com.tuisongbao.engine.chat.conversation.event.ChatConversationResetUnreadEvent;
import com.tuisongbao.engine.chat.conversation.event.handler.ChatConversationDeleteEventHandler;
import com.tuisongbao.engine.chat.conversation.event.handler.ChatConversationGetEventHandler;
import com.tuisongbao.engine.chat.conversation.event.handler.ChatConversationResetUnreadEventHandler;
import com.tuisongbao.engine.chat.db.ChatConversationDataSource;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageGetData;
import com.tuisongbao.engine.chat.message.event.ChatMessageGetEvent;
import com.tuisongbao.engine.chat.message.event.handler.ChatMessageGetEventHandler;
import com.tuisongbao.engine.chat.message.event.handler.ChatMessageMultiGetEventHandler;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.common.BaseManager;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.TSBEngineConstants;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.StrUtil;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ChatConversationManager extends BaseManager {
    private ChatManager mChatManager;
    private ChatConversationDataSource dataSource;

    public ChatConversationManager(TSBEngine engine) {
        mChatManager = engine.chatManager;
        if (mChatManager.isCacheEnabled()) {
            dataSource = new ChatConversationDataSource(TSBEngine.getContext(), engine);
        }
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
            TSBEngineCallback<List<ChatConversation>> callback) {
        try {
            if (!mChatManager.hasLogin()) {
                handleErrorMessage(callback,
                        TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                        "permission denny: need to login");
                return;
            }

            String lastActiveAt = null;
            if (dataSource != null) {
                dataSource.open();
                String userId = mChatManager.getChatUser().getUserId();
                lastActiveAt = dataSource.getLatestLastActiveAt(userId);
                dataSource.close();
            }
            sendRequestOfGetConversations(chatType, target, lastActiveAt, callback);
        } catch (Exception e) {
            handleErrorMessage(callback, Protocol.ENGINE_CODE_UNKNOWN, Protocol.ENGINE_MESSAGE_UNKNOWN_ERROR);
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
            if (!mChatManager.hasLogin()) {
                return;
            }
            if (chatType == null || StrUtil.isEmpty(target)) {
                return;
            }

            if (dataSource != null) {
                // Reset unread event has no response from server, so directly update database.
                String userId = mChatManager.getChatUser().getUserId();
                dataSource.open();
                dataSource.resetUnread(userId, chatType, target);
                dataSource.close();
            }

            ChatConversationResetUnreadEvent message = new ChatConversationResetUnreadEvent();
            ChatConversationData data = new ChatConversationData();
            data.setType(chatType);
            data.setTarget(target);
            message.setData(data);
            ChatConversationResetUnreadEventHandler response = new ChatConversationResetUnreadEventHandler();
            response.setCallback(callback);
            send(message, response);

        } catch (Exception e) {
            handleErrorMessage(callback, Protocol.ENGINE_CODE_UNKNOWN, Protocol.ENGINE_MESSAGE_UNKNOWN_ERROR);
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
            if (!mChatManager.hasLogin()) {
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
            ChatConversationDeleteEvent message = new ChatConversationDeleteEvent();
            ChatConversationData data = new ChatConversationData();
            data.setType(chatType);
            data.setTarget(target);
            message.setData(data);
            ChatConversationDeleteEventHandler response = new ChatConversationDeleteEventHandler();
            response.setCallback(callback);
            send(message, response);

        } catch (Exception e) {
            handleErrorMessage(callback, Protocol.ENGINE_CODE_UNKNOWN, Protocol.ENGINE_MESSAGE_UNKNOWN_ERROR);
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
            TSBEngineCallback<List<ChatMessage>> callback) {
        try {
            if (!mChatManager.hasLogin()) {
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
                callback.onSuccess(new ArrayList<ChatMessage>());
                return;
            }

            if (dataSource == null) {
                ChatMessageGetEvent message = getRequestOfGetMessages(chatType, target, startMessageId, endMessageId, limit);
                ChatMessageGetEventHandler response = new ChatMessageGetEventHandler();
                response.setCallback(callback);
                send(message, response);
                return;
            }

            requestMissingMessagesInLocalCache(chatType, target, startMessageId, endMessageId, limit, callback);
        } catch (Exception e) {
            handleErrorMessage(callback, Protocol.ENGINE_CODE_UNKNOWN, Protocol.ENGINE_MESSAGE_UNKNOWN_ERROR);
            LogUtil.error(LogUtil.LOG_TAG_UNCAUGHT_EX, e);
        }
    }

    public void sendMessage(final ChatMessage message, final TSBEngineCallback<ChatMessage> callback, ChatOptions options) {
        mChatManager.sendMessage(message, callback, options);
    }

    /***
     * Remove all conversations and related messages from local database.
     */
    public void clearCache() {
        dataSource.deleteAllData();
    }

    private void requestMissingMessagesInLocalCache(ChatType chatType, String target, Long startMessageId,
            Long endMessageId, int limit, TSBEngineCallback<List<ChatMessage>> callback) throws JSONException {
        ChatMessageMultiGetEventHandler response = new ChatMessageMultiGetEventHandler();
        response.setMessageIdSpan(startMessageId, endMessageId);
        response.setCallback(callback);

        String currentUserId = mChatManager.getChatUser().getUserId();
        // Query local data
        dataSource.open();
        List<ChatMessage> messages = dataSource.getMessages(currentUserId, chatType, target, startMessageId, endMessageId, limit);
        LogUtil.debug(LogUtil.LOG_TAG_CHAT_CACHE, "Get " + messages.size() + " messages");
        dataSource.close();

        // if startMessageId is null, pull the latest messages.
        if (messages.size() < 1 || startMessageId == null) {
            ChatMessageGetEvent message = getRequestOfGetMessages(chatType, target, startMessageId, endMessageId, limit);
            response.incRequestCount();
            send(message, response);
            return;
        }

        // Check whether missing messages from begin.
        Long maxCachedMessageId = messages.get(0).getMessageId();
        if (startMessageId != null && maxCachedMessageId < startMessageId) {
            ChatMessageGetEvent message = getRequestOfGetMessages(chatType, target, startMessageId, maxCachedMessageId, limit);
            response.incRequestCount();
            send(message, response);
        }
        // Check whether missing messages from end.
        Long minCachedMessageId = messages.get(messages.size() - 1).getMessageId();
        if (endMessageId != null && minCachedMessageId > endMessageId) {
            ChatMessageGetEvent message = getRequestOfGetMessages(chatType, target, minCachedMessageId, endMessageId, limit);
            response.incRequestCount();
            send(message, response);
        }
        // Check missing messages between messages of local DB
        Long pre = maxCachedMessageId;
        for (int i = 1; i < messages.size(); i++) {
            Long next = messages.get(i).getMessageId();
            boolean needSendRequest = (pre - next) > 1;
            if (needSendRequest) {
                ChatMessageGetEvent message = getRequestOfGetMessages(chatType, target, pre, next, limit);
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

    private ChatMessageGetEvent getRequestOfGetMessages(ChatType chatType, String target, Long startMessageId,
            Long endMessageId, int limit) {
        ChatMessageGetEvent message = new ChatMessageGetEvent();
        ChatMessageGetData data = new ChatMessageGetData();
        data.setType(chatType);
        data.setTarget(target);
        data.setStartMessageId(startMessageId);
        data.setEndMessageId(endMessageId);
        data.setLimit(limit);
        message.setData(data);

        return message;
    }

    private void sendRequestOfGetConversations(ChatType chatType, String target, String lastActiveAt,
            TSBEngineCallback<List<ChatConversation>> callback) throws JSONException {
        ChatConversationGetEvent message = new ChatConversationGetEvent();
        ChatConversationData data = new ChatConversationData();
        data.setType(chatType);
        data.setTarget(target);
        // Only query the changes after this time.
        data.setLastActiveAt(lastActiveAt);
        message.setData(data);
        ChatConversationGetEventHandler response = new ChatConversationGetEventHandler();
        response.setCallback(callback);
        send(message, response);
    }
}
