package com.tuisongbao.engine.chat.conversation;

import com.google.gson.JsonObject;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.chat.message.ChatMessage;
import com.tuisongbao.engine.chat.message.ChatMessageContent;
import com.tuisongbao.engine.chat.message.ChatMessageImageContent;
import com.tuisongbao.engine.common.EventEmitter;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.utils.LogUtils;

import java.util.List;

/**
 * <STRONG>会话类</STRONG>
 *
 * <UL>
 *     <LI>直接在该对象上进行发送消息，获取消息，重置未读消息，删除会话等操作</LI>
 *     <LI>绑定 {@link #EVENT_MESSAGE_NEW} 事件可监听新消息</LI>
 *     <LI>开启缓存时，所有的 API 调用会根据缓存数据适当从服务器获取最新的数据，减少流量</LI>
 * </UL>
 */
public class ChatConversation extends EventEmitter {
    /**
     * 新消息事件，监听该事件，可以实时获取当前会话的新消息
     *
     * <pre>
     *    conversation.bind(ChatConversation.EVENT_MESSAGE_NEW, new Emitter.Listener() {
     *        &#64;Override
     *        public void call(final Object... args) {
     *            ChatMessage message = (ChatMessage)args[0];
     *            Log.i(TAG, "当前会话收到新消息 " + message);
     *        }
     *    });
     * </pre>
     */
    transient public static final String EVENT_MESSAGE_NEW = "message:new";
    /**
     * 会话变更事件，监听该事件，可以实时获取当前会话的变更信息。变更的字段只支持 extra 和 lastActiveAt。
     *
     * <pre>
     *    conversation.bind(ChatConversation.EVENT_CHANGED, new Emitter.Listener() {
     *        &#64;Override
     *        public void call(final Object... args) {
     *            Log.i(TAG, "当前会话有更新");
     *            ChatConversation conversation = (ChatConversation)args[0];
     *            JsonObject extra = conversation.getExtra();
     *            Log.i(TAG, "获得新的 extra");
     *            String lastActiveAt = conversation.getLastActiveAt();
     *            Log.i(TAG, "获得新的 lastActiveAt");
     *        }
     *    });
     * </pre>
     */
    transient public static final String EVENT_CHANGED = "changed";
    transient private static final String TAG = ChatConversation.class.getSimpleName();

    private ChatType type;
    private String target;
    private int unreadMessageCount;
    private ChatMessage lastMessage;
    private JsonObject extra;
    private String lastActiveAt;

    transient private Engine mEngine;
    transient private ChatConversationManager mConversationManager;
    /**
     * 拓展项，用于挂载你需要的字段，比如会话相关联的群组的名称。
     */
    transient public Object extension;

    public ChatConversation(Engine engine) {
        mEngine = engine;
        mConversationManager = engine.getChatManager().getConversationManager();
    }

    public ChatType getType() {
        return type;
    }

    public void setType(ChatType type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public void incUnreadMessageCount() {
        unreadMessageCount++;
    }

    public JsonObject getExtra() {
        return extra;
    }

    public void setExtra(JsonObject extra) {
        this.extra = extra;
    }

    public String getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(String lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    /**
     * 获取最后一条消息。
     *
     * <P>
     *      <STRONG>不是</STRONG>发送请求获取最后一条消息，是其本身的一个属性，表示获得该会话时，那一刻的最新的一条消息。
     * </P>

     * @return 最后一条消息
     */
    public ChatMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(ChatMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    /**
     * 重置未读消息。
     * 将未读消息数设置为 0。
     *
     * @param callback 可选
     */
    public void resetUnread(EngineCallback<String> callback) {
        mConversationManager.resetUnread(type, target, callback);
    }

    /**
     * 删除会话
     *
     * @param callback 可选
     */
    public void delete(EngineCallback<String> callback) {
        mConversationManager.delete(type, target, callback);
    }

    /**
     * 获取会话的历史消息
     *
     * messageId 是一个自增数，从 0 开始，数字越大表示消息越晚（新）。
     * startMessageId 和 endMessageId 都可选，参数取值说明，按照顺序依次为 (startMessageId, endMessageId, limit)：
     * <UL>
     *     <LI>(null, null, 20) 获取最新的 20 条消息</LI>
     *     <LI>(null, 40, 20) 如果最新的 messageId 是 50，返回 50 ～ 40；如果是 70, 返回 70 ~ 50</LI>
     *     <LI>(40, null, 20) 获取 40 ~ 20 之间的消息</LI>
     *     <LI>(40, 10, 30) 获取 40 ~ 10 之间的消息</LI>
     * </UL>
     *
     * @param startMessageId    起始 messageId
     * @param endMessageId      结束的 messageId
     * @param limit             必填，消息条数限制
     * @param callback          处理方法
     */
    public void getMessages(Long startMessageId,Long endMessageId, int limit,
            EngineCallback<List<ChatMessage>> callback) {
        mConversationManager.getMessages(type, target, startMessageId, endMessageId, limit, callback);
    }

    /**
     * 在会话中发送消息
     *
     * @param content           消息实体
     * @param callback          结果处理方法
     * @param progressCallback  进度处理方法
     *
     * @return ChatMessage 实例。当为发送图片时，会将缩略图的信息填入，方便开发者刷新页面。
     */
    public ChatMessage sendMessage(ChatMessageContent content, EngineCallback<ChatMessage> callback, ProgressCallback progressCallback) {
        try {
            ChatMessage message = new ChatMessage(mEngine);
            message.setContent(content)
                    .setChatType(type)
                    .setRecipient(target)
                    .setFrom(mEngine.getChatManager().getChatUser().getUserId());
            if (content.getType() == ChatMessage.TYPE.IMAGE) {
                ((ChatMessageImageContent)content).generateThumbnail(200);
            }
            return mEngine.getChatManager().getMessageManager().sendMessage(message, callback, progressCallback);
        } catch (Exception e) {
            LogUtils.error(TAG, e);
            callback.onError(mEngine.getUnhandledResponseError());
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("ChatConversation[type: %s, target: %s, unreadMessage: %d, lastMessage: %s, extra: %s, lastActiveAt: %s]"
                , type.getName(), target, unreadMessageCount, lastMessage, extra, lastActiveAt);
    }
}
