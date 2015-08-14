package com.tuisongbao.engine.chat;

import com.google.gson.Gson;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.conversation.ChatConversationManager;
import com.tuisongbao.engine.chat.group.ChatGroupManager;
import com.tuisongbao.engine.chat.message.ChatMessageManager;
import com.tuisongbao.engine.chat.message.event.handler.ChatMessageNewEventHandler;
import com.tuisongbao.engine.chat.user.entity.ChatLoginData;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.chat.user.event.ChatLoginEvent;
import com.tuisongbao.engine.chat.user.event.ChatLogoutEvent;
import com.tuisongbao.engine.chat.user.event.handler.ChatLoginEventHandler;
import com.tuisongbao.engine.chat.user.event.handler.ChatUserPresenceChangedEventHandler;
import com.tuisongbao.engine.common.BaseManager;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;
import com.tuisongbao.engine.http.HttpConstants;
import com.tuisongbao.engine.http.request.BaseRequest;
import com.tuisongbao.engine.http.response.BaseResponse;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.utils.ExecutorUtils;
import com.tuisongbao.engine.utils.StrUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <STRONG>Chat 管理类</STRONG>
 *
 * <P>
 *     推送宝 {@link Engine} 中，Chat 模块的管理类。
 *     可通过调用 {@link Engine#getChatManager()} 获得该实例。
 *     创建并管理 {@link ChatGroupManager}, {@link ChatMessageManager} 和 {@link ChatConversationManager}。
 *     通过该类 {@link #login(String)} 和 {@link #logout(EngineCallback)}。
 *     在用户已登录的情况下，使用 {@link #bind(String, Listener)} 方法可以获取以下事件的回调通知：
 *
 * <UL>
 *     <LI>{@link #EVENT_LOGIN_SUCCEEDED}</LI>
 *     <LI>{@link #EVENT_LOGIN_FAILED}</LI>
 *     <LI>{@link #EVENT_MESSAGE_NEW}</LI>
 *     <LI>{@link #EVENT_PRESENCE_CHANGED}</LI>
 * </UL>
 *
 * @author Katherine Zhu
 */
public final class ChatManager extends BaseManager {
    /**
     * 有新消息时触发该事件，事件回调接收一个参数，类型为 {@link com.tuisongbao.engine.chat.message.entity.ChatMessage}
     */
    public static final String EVENT_MESSAGE_NEW = "message:new";
    /**
     * 用户上下线通知的提醒，事件回调接收一个参数，类型为 {@link com.tuisongbao.engine.chat.user.entity.ChatUserPresence}
     */
    public static final String EVENT_PRESENCE_CHANGED = "user:presenceChanged";
    /**
     * 登录成功时会触发该事件，包括自动登录 {@link #login(String)} 成功时也会触发该事件，事件回调接收一个参数，类型为 {@link ChatUser}
     */
    public static final String EVENT_LOGIN_SUCCEEDED = "login:succeeded";
    /**
     * 登录失败时会触发该事件，事件回调接收一个参数，类型为 {@link ResponseError}
     */
    public static final String EVENT_LOGIN_FAILED = "login:failed";

    private static final String TAG = "TSB" + ChatManager.class.getSimpleName();

    private ChatGroupManager groupManager;
    private ChatConversationManager conversationManager;
    private ChatMessageManager messageManager;

    private ChatUser mChatUser;
    private boolean mIsCacheEnabled = true;
    private String mUserData;
    private EngineCallback mAuthCallback;

    private final Thread retryEventsThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Iterator<BaseEvent> events = pendingEvents.keySet().iterator();
            while (events.hasNext()) {
                BaseEvent event = events.next();
                BaseEventHandler handler = pendingEvents.get(event);
                send(event, handler);
            }
        }
    });

    public ChatManager(Engine engine) {
        super(engine);
    }

    /**
     * 获取当前用户
     *
     * @return 当前用户；如果用户没有登录成功，返回 {@code null}
     */
    public ChatUser getChatUser() {
        return mChatUser;
    }

    /**
     * 登录聊天系统。
     *
     * <P>
     *     登录过程中，会根据 {@link com.tuisongbao.engine.EngineOptions} 中提供的 {@link com.tuisongbao.engine.EngineOptions#mAuthEndPoint} 进行鉴权。
     *     需通过绑定 {@link #EVENT_LOGIN_SUCCEEDED} 和 {@link #EVENT_LOGIN_FAILED} 事件获取登录结果。
     *
     * @param userData 用户的唯一标识
     */
    public void login(final String userData) {
        try {
            if (StrUtils.isEqual(userData, mUserData)) {
                trigger(EVENT_LOGIN_SUCCEEDED, getChatUser());
                LogUtil.warn(TAG, "Duplicate login");
                return;
            } else {
                // Stop retrying failed events when switching user.
                failedAllPendingEvents();
            }

            mAuthCallback = new EngineCallback<ChatLoginData>() {

                @Override
                public void onSuccess(ChatLoginData data) {
                    mUserData = userData;

                    LogUtil.verbose(TAG, "Auth success");
                    ChatLoginEvent event = new ChatLoginEvent();
                    event.setData(data);
                    ChatLoginEventHandler handler = new ChatLoginEventHandler();
                    send(event, handler);
                }

                @Override
                public void onError(ResponseError error) {
                    LogUtil.verbose(TAG, error.getMessage());
                    onLoginFailed(error);
                    onLogout();
                }
            };

            if (engine.getConnection().isConnected()) {
                auth(userData, mAuthCallback);
            }
        } catch (Exception e) {
            LogUtil.error(TAG, e);
            trigger(EVENT_LOGIN_FAILED, engine.getUnhandledResponseError());
        }
    }

    /**
     * 退出登录，并解绑所有挂载在 ChatManager 上的事件处理方法。
     *
     * @param callback 处理方法
     */
    public void logout(EngineCallback<String> callback) {
        try {
            if (!hasLogin()) {
                return;
            }
            onLogout();
            ChatLogoutEvent event = new ChatLogoutEvent();
            send(event, null);
            callback.onSuccess("OK");
        } catch (Exception e) {
            LogUtil.error(TAG, e);
            callback.onError(engine.getUnhandledResponseError());
        }
    }

    /**
     * 开启缓存，默认是开启状态
     */
    public void enableCache() {
        mIsCacheEnabled = true;
    }

    /**
     * 关闭缓存，默认是开启状态
     */
    public void disableCache() {
        mIsCacheEnabled = false;
    }

    public boolean isCacheEnabled() {
        return mIsCacheEnabled;
    }

    /**
     * 清除所有缓存数据
     */
    public void clearCache() {
        try {
            groupManager.clearCache();
            conversationManager.clearCache();
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
    }

    public boolean hasLogin() {
        return mChatUser != null;
    }

    public void onLoginFailed(ResponseError error) {
        mAuthCallback = null;

        trigger(EVENT_LOGIN_FAILED, error);
    }

    private void onLogout() {
        mChatUser = null;

        mUserData = null;
        mAuthCallback = null;

        unbind(Protocol.EVENT_NAME_MESSAGE_NEW);
        unbind(Protocol.EVENT_NAME_USER_PRESENCE_CHANGE);

        unbind(EVENT_MESSAGE_NEW);
        unbind(EVENT_PRESENCE_CHANGED);

        groupManager = null;
        conversationManager = null;
    }

    /**
     * 获取 {@link com.tuisongbao.engine.chat.conversation.entity.ChatConversation} 的管理类，同一个 ChatManager 上返回的是同一个引用
     *
     * @return ChatConversationManager 实例
     */
    public ChatConversationManager getConversationManager() {
        return conversationManager;
    }

    /**
     * 获取 {@link com.tuisongbao.engine.chat.group.entity.ChatGroup} 的管理类，同一个 ChatManager 上返回的是同一个引用
     *
     * @return ChatGroupManager 实例
     */
    public ChatGroupManager getGroupManager() {
        return groupManager;
    }

    public ChatMessageManager getMessageManager() {
        return messageManager;
    }

    /**
     * 如果发送 Event 不成功，会一直尝试。切换用户之后 {@link #login(String)}，会将所有还未发送成功的 Event 清空。
     *
     * @param event
     * @param handler
     * @return
     */
    @Override
    public boolean send(BaseEvent event, BaseEventHandler handler) {
        try {
            boolean sent = super.send(event, handler);
            if (!sent) {
                addFailedEvent(event, handler);
            } else {
                // Pull event out from pendingEvents
                pendingEvents.remove(event, handler);
            }
        } catch (Exception e1) {
            addFailedEvent(event, handler);
        }
        return true;
    }

    private void addFailedEvent(BaseEvent event, BaseEventHandler handler) {
        LogUtil.error(TAG, "Failed to send event " + event.getName());

        // Avoid duplicated put
        if (pendingEvents.get(event) == null) {
            backoffGap = 0;
            pendingEvents.put(event, handler);
        }
        try {
            retryEventsThread.sleep(backoffGap);
            retryEventsThread.start();
            backoffGap = Math.min(backoffGapMax, backoffGap * 2);
        } catch (Exception e2) {
            LogUtil.error(TAG, e2);
        }
    }

    private void failedAllPendingEvents() {
        try {
            retryEventsThread.interrupt();

            Iterator<BaseEvent> events = pendingEvents.keySet().iterator();
            while (events.hasNext()) {
                BaseEvent event = events.next();
                BaseEventHandler handler = pendingEvents.get(event);
                ResponseError error = new ResponseError();
                error.setMessage("Network issue, request failed");
                // TODO: 15-8-13 How??
//                handler.getCallback().onError(error);
            }

            pendingEvents = new ConcurrentHashMap<>();
            backoffGap = 1;
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
    }

    @Override
    protected void connected() {
        super.connected();

        if (hasLogin()) {
            // Auto login if connection is available.
            auth(mUserData, mAuthCallback);
        }
    }

    @Override
    protected void disconnected() {
        super.disconnected();
        failedAllPendingEvents();
    }

    private JSONObject getAuthParams(String userData) {
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("socketId", engine.getConnection().getSocketId());
            requestData.put("chatLogin", true);
            if (userData != null) {
                requestData.put("authData", userData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestData;
    }

    private void auth(final String userData, final EngineCallback callback) {
        ExecutorUtils.getThreadQueue().execute(new Runnable() {

            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                String authRequestData = getAuthParams(userData).toString();
                BaseRequest request = new BaseRequest(HttpConstants.HTTP_METHOD_POST, engine.getEngineOptions().getAuthEndpoint(),
                        authRequestData);
                BaseResponse response = request.execute();
                if (response == null || !response.isStatusOk()) {
                    ResponseError error = new ResponseError();
                    error.setMessage("Auth failed, connection to user server error or user server feed back error");
                    callback.onError(error);
                    return;
                }
                JSONObject jsonData = response.getJSONData();
                if (jsonData == null) {
                    ResponseError error = new ResponseError();
                    error.setMessage("Auth failed, auth data from auth endpoint is empty");
                    callback.onError(error);
                    return;
                }
                ChatLoginData data = new Gson().fromJson(jsonData.toString(), ChatLoginData.class);
                callback.onSuccess(data);
            }
        });
    }

    public void onLoginSuccess(ChatUser user) {
        mChatUser = user;

        // Init groups and conversations
        groupManager = new ChatGroupManager(engine);
        conversationManager = new ChatConversationManager(engine);
        messageManager = new ChatMessageManager(engine);

        bind(Protocol.EVENT_NAME_MESSAGE_NEW, new ChatMessageNewEventHandler(engine));
        bind(Protocol.EVENT_NAME_USER_PRESENCE_CHANGE, new ChatUserPresenceChangedEventHandler(engine));

        trigger(EVENT_LOGIN_SUCCEEDED, mChatUser);
    }
}
