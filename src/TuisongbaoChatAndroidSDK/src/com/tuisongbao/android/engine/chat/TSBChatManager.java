package com.tuisongbao.android.engine.chat;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.tuisongbao.android.engine.EngineConfig;
import com.tuisongbao.android.engine.TSBEngine;
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
import com.tuisongbao.android.engine.chat.entity.TSBChatUser;
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
import com.tuisongbao.android.engine.common.BaseManager;
import com.tuisongbao.android.engine.common.TSBEngineBindCallback;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.common.TSBResponseMessage;
import com.tuisongbao.android.engine.connection.entity.TSBConnection;
import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.entity.TSBEngineConstants;
import com.tuisongbao.android.engine.http.HttpConstants;
import com.tuisongbao.android.engine.http.request.BaseRequest;
import com.tuisongbao.android.engine.http.response.BaseResponse;
import com.tuisongbao.android.engine.util.ExecutorUtil;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBChatManager extends BaseManager {

    private TSBChatLoginData mTSBChatLoginData;
    private TSBChatLoginMessage mTSBLoginMessage;

    private static TSBChatManager mInstance;

    public static TSBChatManager getInstance() {
        if (mInstance == null) {
            mInstance = new TSBChatManager();
        }
        return mInstance;
    }

    private TSBChatManager() {
        super();
    }

    /**
     * 聊天登录
     * 
     * @param userData 用户信息
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
            auth(message);
            mTSBLoginMessage = message;
        } else {
            callback.onError(
                    TSBEngineConstants.CONNECTION_CODE_CONNECTION_SEND_MESSAGE_FAILED,
                    "can't connect to engine server");
        }
    }

    /**
     * 聊天登出
     * 
     * @param callback
     */
    public void logout(TSBEngineCallback<String> callback) {
        if (!isLogin()) {
            callback.onSuccess("logout success");
            logout();
            return;
        }
        if (TSBEngine.isConnected()) {
            TSBChatLogoutMessage message = new TSBChatLogoutMessage();
            message.setCallback(callback);
            bind(TSBChatLoginMessage.NAME, mLoginEngineCallback);
            TSBResponseMessage response = new TSBResponseMessage();
            response.setCallback(callback);
            send(message, response);
        } else {
            callback.onError(
                    TSBEngineConstants.CONNECTION_CODE_CONNECTION_SEND_MESSAGE_FAILED,
                    "can't connect to engine server");
        }
    }

    /**
     * 获取群组列表
     * 
     * @param groupId 可选，根据 id 过滤
     * @param groupName 可选，根据 name 过滤
     * @param callback
     */
    public void getGroups(String groupId, String groupName, TSBEngineCallback<List<TSBChatGroup>> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback, TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY, "permission denny: need to login");
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
    public void getUsers(String groupId, TSBEngineCallback<List<TSBChatGroupUser>> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback, TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY, "permission denny: need to login");
            return;
        }
        if (StrUtil.isEmpty(groupId)) {
            handleErrorMessage(callback, TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER, "illegal parameter: group id can't be not empty");
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
     * @param groupName 群的名称
     *      注意, 群名称不能以 private- 或者 present- 开头
     * @param members 群聊成员，可以为空，此时成员只有自己
     * @param callback
     */
    public void createGroup(String groupName, List<String> members, TSBEngineCallback<TSBChatGroup> callback) {
        createGroup(groupName, "", members, false, false, callback);
    }

    /**
     * 创建群组
     * 
     * @param groupName 群的名称
     *      注意, 群名称不能以 private- 或者 present- 开头
     * @param description 群的描述
     * @param members 群聊成员，可以为空，此时成员只有自己
     * @param isPublic 默认值 true ，任何用户的加群请求都会直接通过，无需审核
     * @param userCanInvite 默认值 true ，除创建者（owner）外，其他群用户也可以发送加群邀请
     * @param callback
     */
    public void createGroup(String groupName, String description,
            List<String> members, boolean isPublic, boolean userCanInvite,
            TSBEngineCallback<TSBChatGroup> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback, TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY, "permission denny: need to login");
            return;
        }
        if (isIllegalGroupName(groupName)) {
            handleErrorMessage(callback, TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER, "illegal parameter: group name can't not be empty or illegal");
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
     * @param groupId 群的id
     * @param userIds 邀请加入的用户id
     * @param callback
     */
    public void joinInvitation(String groupId, List<String> userIds, TSBEngineCallback<String> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback, TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY, "permission denny: need to login");
            return;
        }
        if (StrUtil.isEmpty(groupId) || userIds == null || userIds.isEmpty()) {
            handleErrorMessage(callback, TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER, "illegal parameter: group id or user ids can't not be empty");
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
     * @param groupId 群的id
     * @param userIds 删除的用户id
     * @param callback
     */
    public void removeUser(String groupId, List<String> userIds, TSBEngineCallback<String> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback, TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY, "permission denny: need to login");
            return;
        }
        if (StrUtil.isEmpty(groupId) || userIds == null || userIds.isEmpty()) {
            handleErrorMessage(callback, TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER, "illegal parameter: group id or user ids can't not be empty");
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
     * @param groupId 群的id
     * @param callback
     */
    public void leaveGroup(String groupId, TSBEngineCallback<String> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback, TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY, "permission denny: need to login");
            return;
        }
        if (StrUtil.isEmpty(groupId)) {
            handleErrorMessage(callback, TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER, "illegal parameter: group id can't not be empty");
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
     * @param type 可选， singleChat（单聊） 或 groupChat （群聊）
     * @param target 可选，跟谁， userId 或 groupId
     * @param callback
     */
    public void getConversation(String type, String target, TSBEngineCallback<TSBChatConversation> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback, TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY, "permission denny: need to login");
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
     * @param type singleChat（单聊） 或 groupChat （群聊）
     * @param target 跟谁， userId 或 groupId
     * @param callback TODO: do not need call back?
     */
    public void resetUnread(String type, String target, TSBEngineCallback<String> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback, TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY, "permission denny: need to login");
            return;
        }
        if (StrUtil.isEmpty(type) || StrUtil.isEmpty(target)) {
            handleErrorMessage(callback, TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER, "illegal parameter: type or target can't not be empty");
            return;
        }
        TSBChatConversationResetUnreadMessage message = new TSBChatConversationResetUnreadMessage();
        TSBChatConversationData data = new TSBChatConversationData();
        data.setType(type);
        data.setTarget(target);
        message.setData(data);
        TSBResponseMessage response = new TSBResponseMessage();
        response.setCallback(callback);
        send(message, response);
    }

    /**
     * 删除会话
     * 
     * @param type singleChat（单聊） 或 groupChat （群聊）
     * @param target 跟谁， userId 或 groupId
     * @param callback
     */
    public void deleteConversation(String type, String target, TSBEngineCallback<String> callback) {
        if (!isLogin()) {
            handleErrorMessage(callback, TSBEngineConstants.TSBENGINE_CODE_PERMISSION_DENNY, "permission denny: need to login");
            return;
        }
        if (StrUtil.isEmpty(type) || StrUtil.isEmpty(target)) {
            handleErrorMessage(callback, TSBEngineConstants.TSBENGINE_CODE_ILLEGAL_PARAMETER, "illegal parameter: type or target can't not be empty");
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

    private void logout() {
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

    private <T> void handleErrorMessage(TSBEngineCallback<T> callback, int code,
            String message) {
        callback.onError(code, message);
    }

    private boolean isLogin() {
        return mTSBChatLoginData != null;
    }

    private void auth(final TSBChatLoginMessage msg) {
        ExecutorUtil.getThreadQueue().execute(new Runnable() {

            @Override
            public void run() {
                TSBChatLoginData data = msg.getData();
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
                        HttpConstants.HTTP_METHOD_POST, EngineConfig.instance()
                                .getAuthEndpoint(), json.toString());
                BaseResponse response = request.execute();
                if (response != null && response.isStatusOk()) {
                    JSONObject jsonData = response.getJSONData();
                    if (jsonData == null) {
                        // feed back empty
                        handleErrorMessage(msg,
                                TSBEngineConstants.TSBENGINE_CHAT_CODE_LOGIN_FAILED,
                                "auth failed, feed back auth data is empty");
                    } else {
                        String signature = jsonData.optString("signature");
                        if (StrUtil.isEmpty(signature)) {
                            // signature data empty
                            handleErrorMessage(msg,
                                    TSBEngineConstants.TSBENGINE_CHAT_CODE_LOGIN_FAILED,
                                    "auth failed, signature is empty");
                        } else {
                            data.setSignature(signature);
                        }
                        String userData = jsonData.optString("userData");
                        data.setUserData(userData);
                        send(msg, new TSBChatLoginResponseMessage());
                    }
                } else {
                    // connection to user server error or user server feed back
                    // error
                    handleErrorMessage(msg,
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
            } else if (TSBChatLogoutMessage.NAME.equals(bindName)) {
                try {
                    JSONObject result = new JSONObject(data);
                    int code = result.optInt(EngineConstants.REQUEST_KEY_CODE);
                    if (EngineConstants.ENGINE_CODE_SUCCESS == code) {
                        logout();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void handleConnect(TSBConnection t) {
        // when logined, it need to re-login
        if (isLogin() && mTSBLoginMessage != null) {
            auth(mTSBLoginMessage);
        }
    }

    @Override
    protected void handleDisconnect(int code, String message) {
        // empty
    }
}
