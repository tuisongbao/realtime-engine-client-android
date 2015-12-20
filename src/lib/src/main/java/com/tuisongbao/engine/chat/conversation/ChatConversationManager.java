package com.tuisongbao.engine.chat.conversation;

import com.github.nkzawa.emitter.Emitter;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.ChatManager;
import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.chat.db.ChatConversationDataSource;
import com.tuisongbao.engine.chat.message.ChatMessage;
import com.tuisongbao.engine.common.BaseManager;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.utils.LogUtils;
import com.tuisongbao.engine.utils.StrUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <STRONG>{@link ChatConversation} 的管理类</STRONG>
 *
 * <UL>
 *     <LI>每个 {@link Engine} 只有一个该实例。</LI>
 *     <LI>所有的API调用会根据缓存数据适当从服务器获取最新的数据，减少流量。</LI>
 * </UL>
 */
public final class ChatConversationManager extends BaseManager {
    /**
     * 新会话事件，该事件触发时表示有新的会话生成，建议开发者调用 {@link #getList(ChatType, String, EngineCallback)} 或者 {@link #getLocalList()}
     * 刷新 UI。
     */
    public static final String EVENT_CONVERSATION_NEW = "conversation:new";
    /**
     * 会话变更事件，该事件触发时表示会话的 extra 等信息有改变，建议开发者调用 {@link #getList(ChatType, String, EngineCallback)} 或者 {@link #getLocalList()}
     * 获取变更后的会话列表。
     */
    public static final String EVENT_CONVERSATION_CHANGED = "conversation:changed";
    private static final String TAG = "TSB" + ChatConversationManager.class.getSimpleName();

    private final ChatManager mChatManager;
    private ChatConversationDataSource dataSource;
    /**
     * Structure:
     *      key: target
     *      value: ChatConversation
     */
    private ConcurrentMap<String, ChatConversation> conversationMap = new ConcurrentHashMap<>();

    public ChatConversationManager(final Engine engine) {
        mChatManager = engine.getChatManager();
        if (mChatManager.isCacheEnabled()) {
            dataSource = new ChatConversationDataSource(Engine.getContext(), engine);
        }
        mChatManager.bind(ChatManager.EVENT_MESSAGE_NEW, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                final ChatMessage message = (ChatMessage) args[0];
                String target = message.getRecipient();
                if (message.getChatType() == ChatType.SingleChat) {
                    target = message.getFrom();
                }
                ChatConversation conversation = conversationMap.get(target);
                if (conversation == null) {
                    final String targetToLoad = target;
                    // Load conversations from server, because this conversation may has extras.
                    sendRequestOfGetConversations(null, targetToLoad, null, new EngineCallback<List<ChatConversation>>() {
                        @Override
                        public void onSuccess(List<ChatConversation> chatConversations) {
                            if (chatConversations.size() > 0) {
                                conversationMap.put(targetToLoad, chatConversations.get(0));
                                triggerNewEvent(targetToLoad, message);
                            }
                        }

                        @Override
                        public void onError(ResponseError error) {
                            LogUtils.error(TAG, error.toString());
                        }
                    });
                } else {
                    triggerNewEvent(target, message);
                }
            }
        });

        bind(Protocol.EVENT_NAME_CONVERSATION_CHANGED, new Listener() {
            @Override
            public void call(Object... args) {
                ChatConversation changedConversation = (ChatConversation)args[0];
                String target = changedConversation.getTarget();
                ChatConversation conversation = conversationMap.get(target);

                conversation.setExtra(changedConversation.getExtra());
                conversation.setLastActiveAt(changedConversation.getLastActiveAt());
                conversationMap.put(target, conversation);

                conversation.trigger(ChatConversation.EVENT_CHANGED, changedConversation);
                trigger(EVENT_CONVERSATION_CHANGED, changedConversation);
            }
        });
    }

    /**
     * 查找会话，不发送请求，只从缓存中查找，不存在时返回 {@code null}。获取最新的数据请使用 {@link #getList(ChatType, String, EngineCallback)}。
     *
     * @param target    必填，userId 或 groupId，表示与谁的会话
     * @return          缓存的会话
     */
    public ChatConversation find(String target) {
        try {
            if (StrUtils.isEmpty(target)) {
                return null;
            }
            ChatConversation conversation = conversationMap.get(target);
            if (conversation != null) {
                return conversation;
            }
        } catch (Exception e) {
            LogUtils.error(TAG, e);
        }
        return null;
    }

    /**
     * 获取本地会话，不发送请求，只从缓存中查找，不存在时会创建一个新会话。
     *
     * @param target    必填，userId 或 groupId，表示与谁的会话
     * @param type      聊天类型
     * @return          缓存的会话或者新会话
     */
    public ChatConversation loadOne(String target, ChatType type) {
        try {
            ChatConversation conversation = find(target);
            if (conversation != null) {
                return conversation;
            }
            conversation = new ChatConversation(engine);
            conversation.setType(type);
            conversation.setTarget(target);
            conversationMap.put(target, conversation);

            return conversation;
        } catch (Exception e) {
            LogUtils.error(TAG, e);
        }
        return null;
    }

    /**
     * 获取缓存的会话列表
     *
     * <P>
     *     建议当收到 {@link #EVENT_CONVERSATION_NEW} 时，调用该方法获取当前最新的会话列表，节省流量。
     * </P>
     *
     * @return  按 {@link ChatConversation#lastActiveAt} 逆序排列的会话列表
     */
    public List<ChatConversation> getLocalList() {
        List<ChatConversation> conversations = new ArrayList<>();
        for (String key: conversationMap.keySet()) {
            conversations.add(conversationMap.get(key));
        }
        Collections.sort(conversations, new ChatConversationSorter());
        return conversations;
    }

    /**
     * 获取会话列表
     *
     * @param chatType {@link ChatType#SingleChat}（单聊） 或 {@link ChatType#GroupChat} （群聊）
     * @param target 可选，userId 或 groupId;表示与谁的会话，为 {@code null} 时表示获取该用户的所有会话
     * @param callback 结果通知函数
     */
    public void getList(ChatType chatType, String target,
            final EngineCallback<List<ChatConversation>> callback) {
        try {
            String lastActiveAt = null;
            if (dataSource != null) {
                dataSource.open();
                String userId = mChatManager.getChatUser().getUserId();
                lastActiveAt = dataSource.getLatestLastActiveAt(userId);
                dataSource.close();
            }
            sendRequestOfGetConversations(chatType, target, lastActiveAt, new EngineCallback<List<ChatConversation>>() {
                @Override
                public void onSuccess(List<ChatConversation> chatConversations) {
                    // Unread message count
                    conversationMap = new ConcurrentHashMap<>();
                    for (ChatConversation conversation : chatConversations) {
                        conversationMap.put(conversation.getTarget(), conversation);
                    }

                    callback.onSuccess(chatConversations);
                }

                @Override
                public void onError(ResponseError error) {
                    callback.onError(error);
                }
            });
        } catch (Exception e) {
            callback.onError(engine.getUnhandledResponseError());
            LogUtils.error(TAG, e);
        }
    }

    /**
     * 重置未读消息数
     *
     * @param chatType  {@link ChatType#SingleChat}（单聊） 或 {@link ChatType#GroupChat} （群聊）
     * @param target    必填，userId 或 groupId
     */
    public void resetUnread(ChatType chatType, String target, EngineCallback<String> callback) {
        try {
            if (!mChatManager.hasLogin()) {
                return;
            }
            if (chatType == null || StrUtils.isEmpty(target)) {
                return;
            }

            // Update local storage
            ChatConversation localConversation = conversationMap.get(target);
            if (localConversation != null) {
                localConversation.setUnreadMessageCount(0);
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
            LogUtils.error(TAG, e);
        }
    }

    /**
     * 删除会话
     *
     * @param chatType  {@link ChatType#SingleChat}（单聊） 或 {@link ChatType#GroupChat} （群聊）
     * @param target    必填，userId 或 groupId
     * @param callback  结果通知函数
     */
    public void delete(ChatType chatType, final String target,
            final EngineCallback<String> callback) {
        try {
            ChatConversationDeleteEvent event = new ChatConversationDeleteEvent();
            ChatConversation data = new ChatConversation(engine);
            data.setType(chatType);
            data.setTarget(target);
            event.setData(data);
            ChatConversationDeleteEventHandler response = new ChatConversationDeleteEventHandler();
            response.setCallback(new EngineCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    // Update local storage
                    conversationMap.remove(target);
                    callback.onSuccess(result);
                }

                @Override
                public void onError(ResponseError error) {
                    callback.onError(error);
                }
            });
            send(event, response);

        } catch (Exception e) {
            callback.onError(engine.getUnhandledResponseError());
            LogUtils.error(TAG, e);
        }
    }

    /**
     * 获取某个会话的历史消息。startMessageId 和 endMessageId 都可选，都为{@code null}时表示获取最新的{@code limit}条消息
     *
     * @param chatType          {@link ChatType#SingleChat}（单聊） 或 {@link ChatType#GroupChat} （群聊）
     * @param target            必填，userId 或 groupId
     * @param startMessageId    起始 messageId
     * @param endMessageId      结束的 messageId
     * @param limit             消息条数限制
     * @param callback          结果通知函数
     */
    public void getMessages(ChatType chatType, String target, Long startMessageId,
            Long endMessageId, int limit,
            EngineCallback<List<ChatMessage>> callback) {
        engine.getChatManager().getMessageManager().getMessages(chatType, target, startMessageId, endMessageId, limit, callback);
    }

    /**
     * 清除所有缓存的会话，包括相关消息
     */
    public void clearCache() {
        try {
            dataSource.open();
            dataSource.deleteAllData();
            dataSource.close();
        } catch (Exception e) {
            LogUtils.error(TAG, e);
        }
    }

    private void sendRequestOfGetConversations(ChatType chatType, String target, String lastActiveAt,
            EngineCallback<List<ChatConversation>> callback) {
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

    private void triggerNewEvent(String target, ChatMessage message) {
        ChatConversation conversation = conversationMap.get(target);
        // When talking with self, do not inc unreadMessageCount
        if (!message.getRecipient().equals(message.getFrom())) {
            conversation.incUnreadMessageCount();
        }
        conversation.setLastMessage(message);
        // Conversations sort by this field
        conversation.setLastActiveAt(message.getCreatedAt());
        conversation.trigger(ChatConversation.EVENT_MESSAGE_NEW, message);
        trigger(EVENT_CONVERSATION_NEW, conversation);
    }
}
