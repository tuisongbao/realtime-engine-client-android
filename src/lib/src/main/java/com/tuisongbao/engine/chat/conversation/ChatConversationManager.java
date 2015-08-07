package com.tuisongbao.engine.chat.conversation;

import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.ChatOptions;
import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
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
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.utils.StrUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ChatConversationManager extends BaseManager {
    private static final String TAG = "TSB" + ChatConversationManager.class.getSimpleName();

    private ChatManager mChatManager;
    private ChatConversationDataSource dataSource;

    public ChatConversationManager(Engine engine) {
        mChatManager = engine.getChatManager();
        if (mChatManager.isCacheEnabled()) {
            dataSource = new ChatConversationDataSource(Engine.getContext(), engine);
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
            EngineCallback<List<ChatConversation>> callback) {
        try {
            String lastActiveAt = null;
            if (dataSource != null) {
                dataSource.open();
                String userId = mChatManager.getChatUser().getUserId();
                lastActiveAt = dataSource.getLatestLastActiveAt(userId);
                dataSource.close();
            }
            sendRequestOfGetConversations(chatType, target, lastActiveAt, callback);
        } catch (Exception e) {
            callback.onError(engine.getUnhandledResponseError());
            LogUtil.error(TAG, e);
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
    public void resetUnread(ChatType chatType, String target, EngineCallback<String> callback) {
        try {
            if (!mChatManager.hasLogin()) {
                return;
            }
            if (chatType == null || StrUtils.isEmpty(target)) {
                return;
            }

            if (dataSource != null) {
                // Reset unread event has no response from server, so directly update database.
                String userId = mChatManager.getChatUser().getUserId();
                dataSource.open();
                dataSource.resetUnread(userId, chatType, target);
                dataSource.close();
            }

            ChatConversationResetUnreadEvent event = new ChatConversationResetUnreadEvent();
            ChatConversation data = new ChatConversation(engine);
            data.setType(chatType);
            data.setTarget(target);
            event.setData(data);
            ChatConversationResetUnreadEventHandler response = new ChatConversationResetUnreadEventHandler();
            response.setCallback(callback);
            send(event, response);

        } catch (Exception e) {
            callback.onError(engine.getUnhandledResponseError());
            LogUtil.error(TAG, e);
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
            EngineCallback<String> callback) {
        try {
            ChatConversationDeleteEvent event = new ChatConversationDeleteEvent();
            ChatConversation data = new ChatConversation(engine);
            data.setType(chatType);
            data.setTarget(target);
            event.setData(data);
            ChatConversationDeleteEventHandler response = new ChatConversationDeleteEventHandler();
            response.setCallback(callback);
            send(event, response);

        } catch (Exception e) {
            callback.onError(engine.getUnhandledResponseError());
            LogUtil.error(TAG, e);
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
            EngineCallback<List<ChatMessage>> callback) {
        try {
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
            callback.onError(engine.getUnhandledResponseError());
            LogUtil.error(TAG, e);
        }
    }

    public void sendMessage(final ChatMessage message, final EngineCallback<ChatMessage> callback, ChatOptions options) {
        mChatManager.getMessageManager().sendMessage(message, callback, options);
    }

    /***
     * Remove all conversations and related messages from local database.
     */
    public void clearCache() {
        try {
            dataSource.open();
            dataSource.deleteAllData();
            dataSource.close();
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
    }

    private void requestMissingMessagesInLocalCache(ChatType chatType, String target, Long startMessageId,
            Long endMessageId, int limit, EngineCallback<List<ChatMessage>> callback) throws JSONException {
        ChatMessageMultiGetEventHandler response = new ChatMessageMultiGetEventHandler();
        response.setMessageIdSpan(startMessageId, endMessageId);
        response.setCallback(callback);

        String currentUserId = mChatManager.getChatUser().getUserId();
        // Query local data
        dataSource.open();
        List<ChatMessage> messages = dataSource.getMessages(currentUserId, chatType, target, startMessageId, endMessageId, limit);
        LogUtil.debug(TAG, "Get " + messages.size() + " messages");
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
        ChatMessageGetEvent event = new ChatMessageGetEvent();
        ChatMessageGetData data = new ChatMessageGetData();
        data.setType(chatType);
        data.setTarget(target);
        data.setStartMessageId(startMessageId);
        data.setEndMessageId(endMessageId);
        data.setLimit(limit);
        event.setData(data);

        return event;
    }

    private void sendRequestOfGetConversations(ChatType chatType, String target, String lastActiveAt,
            EngineCallback<List<ChatConversation>> callback) throws JSONException {
        ChatConversationGetEvent event = new ChatConversationGetEvent();
        ChatConversation data = new ChatConversation(engine);
        data.setType(chatType);
        data.setTarget(target);
        // Only query the changes after this time.
        data.setLastActiveAt(lastActiveAt);
        event.setData(data);
        ChatConversationGetEventHandler response = new ChatConversationGetEventHandler();
        response.setCallback(callback);
        send(event, response);
    }
}
