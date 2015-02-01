package com.tuisongbao.android.engine.chat;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.entity.ChatType;
import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.chat.entity.TSBChatConversationData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroup;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupCreateData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupGetData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupGetUsersData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupJoinInvitationData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupLeaveData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupRemoveUserData;
import com.tuisongbao.android.engine.chat.entity.TSBChatGroupUser;
import com.tuisongbao.android.engine.chat.entity.TSBChatLoginData;
import com.tuisongbao.android.engine.chat.entity.TSBChatMessageGetData;
import com.tuisongbao.android.engine.chat.entity.TSBChatMessageSendData;
import com.tuisongbao.android.engine.chat.entity.TSBChatUser;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.entity.TSBMessageBody;
import com.tuisongbao.android.engine.chat.message.TSBChatConversationDeleteMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatConversationGetMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatConversationGetReponseMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatConversationResetUnreadMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupCreateMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupCreateReponseMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupGetMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupGetReponseMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupGetUsersMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupGetUsersReponseMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupJoinInvitationMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupLeaveMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatGroupRemoveUserMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatLoginMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatLoginResponseMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatLogoutMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatMessageGetMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatMessageGetResponseMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatMessageResponseMessage;
import com.tuisongbao.android.engine.chat.message.TSBChatMessageResponseMessage.TSBChatMessageResponseMessageCallback;
import com.tuisongbao.android.engine.chat.message.TSBChatMessageSendMessage;
import com.tuisongbao.android.engine.chat.serializer.TSBChatMessageBodySerializer;
import com.tuisongbao.android.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.android.engine.chat.serializer.TSBChatMessageTypeSerializer;
import com.tuisongbao.android.engine.common.BaseManager;
import com.tuisongbao.android.engine.common.TSBEngineBindCallback;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.common.TSBEngineResponseToServerRequestMessage;
import com.tuisongbao.android.engine.common.TSBResponseMessage;
import com.tuisongbao.android.engine.connection.entity.TSBConnection;
import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.entity.TSBEngineConstants;
import com.tuisongbao.android.engine.http.HttpConstants;
import com.tuisongbao.android.engine.http.request.BaseRequest;
import com.tuisongbao.android.engine.http.response.BaseResponse;
import com.tuisongbao.android.engine.service.EngineServiceManager;
import com.tuisongbao.android.engine.util.ExecutorUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBChatManager extends BaseManager {

    private TSBChatLoginData mTSBChatLoginData;
    private TSBChatLoginMessage mTSBLoginMessage;

    private static TSBChatManager mInstance;

    public synchronized static TSBChatManager getInstance() {
        if (mInstance == null) {
            mInstance = new TSBChatManager();
        }
        return mInstance;
    }

    private TSBChatManager() {
        super();
        // bind get message event
        TSBChatMessageResponseMessage response = new TSBChatMessageResponseMessage(
                mChatMessageCallback);
        bind(TSBChatMessageGetMessage.NAME, response);
        // bind receive new message event
        bind(EngineConstants.CHAT_NAME_NEW_MESSAGE, response);
    }

    /**
     * 聊天登录
     * 
     * @param userData
     *            用户信息
     * @param callback
     */
    public void login(String userData, TSBEngineCallback<TSBChatUser> callback) {
        if (isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CHAT_CODE_LOGIN_HAS_LOGINED,
                    "user has been logined");
            return;
        }
        if (TSBEngine.isConnected()) {
            TSBChatLoginMessage message = new TSBChatLoginMessage();
            TSBChatLoginData data = new TSBChatLoginData();
            data.setUserData(userData);
            message.setData(data);
            message.setCallback(callback);
            bind(TSBChatLoginMessage.NAME, mLoginEngineCallback);
            auth(message, true);
            mTSBLoginMessage = message;
        } else {
            callback.onError(
                    TSBEngineConstants.CONNECTION_CODE_CONNECTION_SEND_MESSAGE_FAILED,
                    "can't connect to engine server");
        }
    }

    /**
     * 退出登录
     * 
     */
    public void logout() {
        if (!isLogin()) {
            clearCache();
            return;
        }
        if (TSBEngine.isConnected()) {
            TSBChatLogoutMessage message = new TSBChatLogoutMessage();
            send(message);
        }
        clearCache();
    }

    /**
     * 获取群组列表
     * 
     * @param groupId
     *            可选，根据 id 过滤
     * @param groupName
     *            可选，根据 name 过滤
     * @param callback
     */
    public void getGroups(String groupId, String groupName,
            TSBEngineCallback<List<TSBChatGroup>> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }
        TSBChatGroupGetMessage message = new TSBChatGroupGetMessage();
        TSBChatGroupGetData data = new TSBChatGroupGetData();
        data.setGroupId(groupId);
        data.setName(groupName);
        message.setData(data);
        TSBChatGroupGetReponseMessage response = new TSBChatGroupGetReponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    /**
     * 获取群组下用户列表
     * 
     * @param groupId
     * @param callback
     */
    public void getUsers(String groupId,
            TSBEngineCallback<List<TSBChatGroupUser>> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }
        if (StrUtil.isEmpty(groupId)) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: group id can't be not empty");
            return;
        }
        TSBChatGroupGetUsersMessage message = new TSBChatGroupGetUsersMessage();
        TSBChatGroupGetUsersData data = new TSBChatGroupGetUsersData();
        data.setGroupId(groupId);
        message.setData(data);
        TSBChatGroupGetUsersReponseMessage response = new TSBChatGroupGetUsersReponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    /**
     * 创建群组
     * 
     * @param groupName
     *            群的名称 注意, 群名称不能以 private- 或者 present- 开头
     * @param members
     *            群聊成员，可以为空，此时成员只有自己
     * @param callback
     */
    public void createGroup(String groupName, List<String> members,
            TSBEngineCallback<TSBChatGroup> callback) {
        createGroup(groupName, "", members, false, false, callback);
    }

    /**
     * 创建群组
     * 
     * @param groupName
     *            群的名称 注意, 群名称不能以 private- 或者 present- 开头
     * @param description
     *            群的描述
     * @param members
     *            群聊成员，可以为空，此时成员只有自己
     * @param isPublic
     *            默认值 true ，任何用户的加群请求都会直接通过，无需审核
     * @param userCanInvite
     *            默认值 true ，除创建者（owner）外，其他群用户也可以发送加群邀请
     * @param callback
     */
    public void createGroup(String groupName, String description,
            List<String> members, boolean isPublic, boolean userCanInvite,
            TSBEngineCallback<TSBChatGroup> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }
        if (!isIllegalGroupName(groupName)) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: group name can't not be empty or illegal");
            return;
        }

        TSBChatGroupCreateMessage message = new TSBChatGroupCreateMessage();
        TSBChatGroupCreateData data = new TSBChatGroupCreateData();
        data.setName(groupName);
        data.setDescription(description);
        data.setInviteUserIds(members);
        data.setPublic(isPublic);
        data.setUserCanInvite(userCanInvite);
        message.setData(data);
        TSBChatGroupCreateReponseMessage response = new TSBChatGroupCreateReponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    /**
     * 邀请加入群组
     * 
     * @param groupId
     *            群的id
     * @param userIds
     *            邀请加入的用户id
     * @param callback
     */
    public void joinInvitation(String groupId, List<String> userIds,
            TSBEngineCallback<String> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }
        if (StrUtil.isEmpty(groupId) || userIds == null || userIds.isEmpty()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: group id or user ids can't not be empty");
            return;
        }

        TSBChatGroupJoinInvitationMessage message = new TSBChatGroupJoinInvitationMessage();
        TSBChatGroupJoinInvitationData data = new TSBChatGroupJoinInvitationData();
        data.setGroupId(groupId);
        data.setUserIds(userIds);
        message.setData(data);
        TSBResponseMessage response = new TSBResponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    /**
     * 删除群组中的用户
     * 
     * @param groupId
     *            群的id
     * @param userIds
     *            删除的用户id
     * @param callback
     */
    public void removeUsers(String groupId, List<String> userIds,
            TSBEngineCallback<String> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }
        if (StrUtil.isEmpty(groupId) || userIds == null || userIds.isEmpty()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: group id or user ids can't not be empty");
            return;
        }

        TSBChatGroupRemoveUserMessage message = new TSBChatGroupRemoveUserMessage();
        TSBChatGroupRemoveUserData data = new TSBChatGroupRemoveUserData();
        data.setGroupId(groupId);
        data.setUserIds(userIds);
        message.setData(data);
        TSBResponseMessage response = new TSBResponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    /**
     * 离开群组
     * 
     * @param groupId
     *            群的id
     * @param callback
     */
    public void leaveGroup(String groupId, TSBEngineCallback<String> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }
        if (StrUtil.isEmpty(groupId)) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: group id can't not be empty");
            return;
        }

        TSBChatGroupLeaveMessage message = new TSBChatGroupLeaveMessage();
        TSBChatGroupLeaveData data = new TSBChatGroupLeaveData();
        data.setGroupId(groupId);
        message.setData(data);
        TSBResponseMessage response = new TSBResponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    /**
     * 获取会话
     * 
     * @param type
     *            可选， singleChat（单聊） 或 groupChat （群聊）
     * @param target
     *            可选，跟谁， userId 或 groupId
     * @param callback
     */
    public void getConversation(ChatType type, String target,
            TSBEngineCallback<List<TSBChatConversation>> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }

        TSBChatConversationGetMessage message = new TSBChatConversationGetMessage();
        TSBChatConversationData data = new TSBChatConversationData();
        data.setType(type);
        data.setTarget(target);
        message.setData(data);
        TSBChatConversationGetReponseMessage response = new TSBChatConversationGetReponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    /**
     * 重置会话
     * 
     * @param type
     *            singleChat（单聊） 或 groupChat （群聊）
     * @param target
     *            跟谁， userId 或 groupId
     */
    public void resetUnread(ChatType type, String target) {
        if (!isLogin()) {
            return;
        }
        if (type == null || StrUtil.isEmpty(target)) {
            return;
        }
        TSBChatConversationResetUnreadMessage message = new TSBChatConversationResetUnreadMessage();
        TSBChatConversationData data = new TSBChatConversationData();
        data.setType(type);
        data.setTarget(target);
        message.setData(data);
        send(message);
    }

    /**
     * 删除会话
     * 
     * @param type
     *            singleChat（单聊） 或 groupChat （群聊）
     * @param target
     *            跟谁， userId 或 groupId
     * @param callback
     */
    public void deleteConversation(ChatType type, String target,
            TSBEngineCallback<String> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }
        if (type == null || StrUtil.isEmpty(target)) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: type or target can't not be empty");
            return;
        }
        TSBChatConversationDeleteMessage message = new TSBChatConversationDeleteMessage();
        TSBChatConversationData data = new TSBChatConversationData();
        data.setType(type);
        data.setTarget(target);
        message.setData(data);
        TSBResponseMessage response = new TSBResponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    /**
     * 获取消息
     * 
     * @param type
     *            singleChat（单聊） 或 groupChat （群聊）
     * @param target
     *            跟谁， userId 或 groupId
     */
    public void getMessages(ChatType type, String target,
            TSBEngineCallback<List<TSBMessage>> callback) {
        getMessages(type, target, 0, 0, 20, callback);
    }

    /**
     * 获取消息
     * 
     * @param type
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
    public void getMessages(ChatType type, String target, long startMessageId,
            long endMessageId, int limit,
            TSBEngineCallback<List<TSBMessage>> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }
        if (type == null) {
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
        TSBChatMessageGetMessage message = new TSBChatMessageGetMessage();
        TSBChatMessageGetData data = new TSBChatMessageGetData();
        data.setType(type);
        data.setTarget(target);
        data.setStartMessageId(startMessageId);
        data.setEndMessageId(endMessageId);
        data.setLimit(limit);
        message.setData(data);

        TSBChatMessageGetResponseMessage response = new TSBChatMessageGetResponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    /**
     * 获取消息
     * 
     * @param type
     *            singleChat（单聊） 或 groupChat （群聊）
     * @param recipientId
     *            跟谁， userId 或 groupId
     * @param startMessageId
     *            可选
     * @param endMessageId
     *            可选
     * @param limit
     *            可选，默认 20，最大 100
     */
    public void sendMessage(TSBMessage message,
            TSBEngineCallback<TSBMessage> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY,
                    "permission denny: need to login");
            return;
        }
        if (message == null) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: message can't not be empty");
            return;
        }
        if (message.getChatType() == null) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: message chat type can't not be empty");
            return;
        }
        if (StrUtil.isEmpty(message.getRecipient())) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: message recipient id can't not be empty");
            return;
        }
        if (message.getBody() == null) {
            handleErrorMessage(callback,
                    TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER,
                    "illegal parameter: message body can't not be empty");
            return;
        }
        TSBChatMessageSendMessage request = new TSBChatMessageSendMessage();
        TSBChatMessageSendData data = new TSBChatMessageSendData();
        data.setTo(message.getRecipient());
        data.setType(message.getChatType());
        data.setContent(message.getBody());
        request.setData(data);

        TSBChatMessageResponseMessage response = new TSBChatMessageResponseMessage(
                mChatMessageCallback);
        response.setSendMessage(message);
        response.setCustomCallback(callback);
        send(request, response);
    }

    public void bind(String bindName, TSBEngineBindCallback callback) {
        if (StrUtil.isEmpty(bindName) || callback == null) {
            return;
        }
        super.bind(bindName, callback);
    }

    public void unbind(String bindName, TSBEngineBindCallback callback) {
        if (StrUtil.isEmpty(bindName) || callback == null) {
            return;
        }
        super.unbind(bindName, callback);
    }

    private void clearCache() {
        mTSBChatLoginData = null;
        mTSBLoginMessage = null;
    }

    private void handleErrorMessage(TSBChatLoginMessage msg, int code,
            String message) {
        handleErrorMessage(msg.getCallback(), code, message);
    }

    private boolean isIllegalGroupName(String groupName) {
        return !StrUtil.isEmpty(groupName)
                && !groupName
                        .startsWith(TSBEngineConstants.TSBENGINE_CHANNEL_PREFIX_PRIVATE)
                && !groupName
                        .startsWith(TSBEngineConstants.TSBENGINE_CHANNEL_PREFIX_PRESENCE);
    }

    private <T> void handleErrorMessage(TSBEngineCallback<T> callback,
            int code, String message) {
        callback.onError(code, message);
    }

    private boolean isLogin() {
        return mTSBChatLoginData != null;
    }

    private void auth(final TSBChatLoginMessage msg, final boolean isNeedCallback) {
        ExecutorUtil.getThreadQueue().execute(new Runnable() {

            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    json.put("socketId", TSBEngine.getSocketId());
                    json.put("chatLogin", true);
                    if (msg.getData() != null
                            && !StrUtil.isEmpty(msg.getData().getUserData())) {
                        json.put("authData", msg.getData().getUserData());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                BaseRequest request = new BaseRequest(
                        HttpConstants.HTTP_METHOD_POST, TSBEngine
                                .getTSBEngineOptions().getAuthEndpoint(), json
                                .toString());
                BaseResponse response = request.execute();
                if (response != null && response.isStatusOk()) {
                    JSONObject jsonData = response.getJSONData();
                    if (jsonData == null) {
                        // feed back empty
                        handleErrorMessage(
                                msg,
                                TSBEngineConstants.TSBENGINE_CHAT_CODE_LOGIN_FAILED,
                                "auth failed, feed back auth data is empty");
                    } else {
                        String signature = jsonData.optString("signature");
                        if (StrUtil.isEmpty(signature)) {
                            // signature data empty
                            handleErrorMessage(
                                    msg,
                                    TSBEngineConstants.TSBENGINE_CHAT_CODE_LOGIN_FAILED,
                                    "auth failed, signature is empty");
                        } else {
                            // empty
                        }
                        String userData = jsonData.optString("userData");
                        TSBChatLoginMessage authMessage = new TSBChatLoginMessage();
                        TSBChatLoginData authData = new TSBChatLoginData();
                        authData.setUserData(userData);
                        authData.setSignature(signature);
                        authMessage.setData(authData);
                        if (isNeedCallback) {
                            TSBChatLoginResponseMessage responseMessage = new TSBChatLoginResponseMessage();
                            responseMessage.setCallback(msg.getCallback());
                            send(authMessage, responseMessage);
                        } else {
                            send(authMessage);
                        }
                    }
                } else {
                    // connection to user server error or user server feed back
                    // error
                    handleErrorMessage(
                            msg,
                            TSBEngineConstants.TSBENGINE_CHAT_CODE_LOGIN_FAILED,
                            "auth failed, connection to user server error or user server feed back error");
                }
            }
        });
    }

    private TSBEngineBindCallback mLoginEngineCallback = new TSBEngineBindCallback() {

        @Override
        public void onEvent(String bindName, String name, String data) {
            // login
            if (TSBChatLoginMessage.NAME.equals(bindName)) {
                mTSBChatLoginData = new Gson().fromJson(data,
                        TSBChatLoginData.class);
            }
        }
    };

    private TSBChatMessageResponseMessageCallback mChatMessageCallback = new TSBChatMessageResponseMessageCallback() {

        @Override
        public void onEvent(TSBChatMessageResponseMessage response) {
            if (response.isSuccess()) {
                if (TSBChatMessageGetMessage.NAME
                        .equals(response.getBindName())) {
                    // 当为获取消息时
                    // response to server
                    long serverRequestId = response.getServerRequestId();
                    TSBEngineResponseToServerRequestMessage message = new TSBEngineResponseToServerRequestMessage(
                            serverRequestId, true);
                    send(message);
                } else if (TSBChatMessageSendMessage.NAME.equals(response
                        .getBindName())) {
                    if (response.getCustomCallback() != null) {
                        // 发送消息成功
                        TSBMessage message = response.getSendMessage();
                        try {
                            JSONObject json = new JSONObject(response.getData());
                            long messageId = json.optLong("messageId");
                            message.setMessageId(messageId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        response.getCustomCallback().onSuccess(message);
                    }
                } else if (EngineConstants.CHAT_NAME_NEW_MESSAGE
                        .equals(response.getBindName())) {
                    // 接收到消息
                    if (response.getData() != null) {
                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.registerTypeAdapter(ChatType.class,
                                new TSBChatMessageChatTypeSerializer());
                        gsonBuilder.registerTypeAdapter(TSBMessage.TYPE.class,
                                new TSBChatMessageTypeSerializer());
                        gsonBuilder.registerTypeAdapter(TSBMessageBody.class,
                                new TSBChatMessageBodySerializer());
                        Gson gson = gsonBuilder.create();
                        TSBMessage message = gson.fromJson(response.getData(),
                                TSBMessage.class);
                        EngineServiceManager.receivedMessage(message);
                        // response to server
                        long serverRequestId = response.getServerRequestId();
                        TSBEngineResponseToServerRequestMessage request = new TSBEngineResponseToServerRequestMessage(
                                serverRequestId, true);
                        try {
                            JSONObject result = new JSONObject();
                            result.put("messageId", message.getMessageId());
                            request.setResult(result);
                            send(request);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                // 发送消息失败
                if (TSBChatMessageSendMessage.NAME.equals(response
                        .getBindName())) {
                    if (response.getCustomCallback() != null) {
                        response.getCustomCallback().onError(response.getCode(), response.getErrorMessage());
                    }
                }
            }

        }
    };

    @Override
    protected void handleConnect(TSBConnection t) {
        // when logined, it need to re-login
        if (isLogin() && mTSBLoginMessage != null) {
            // 当断掉重连时不需要回调
            auth(mTSBLoginMessage, false);
        }
    }

    @Override
    protected void handleDisconnect(int code, String message) {
        // empty
    }
}
