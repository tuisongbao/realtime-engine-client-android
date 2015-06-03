package com.tuisongbao.android.engine.chat.message;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.db.TSBConversationDataSource;
import com.tuisongbao.android.engine.chat.entity.ChatType;
import com.tuisongbao.android.engine.chat.entity.TSBMediaMessageBody;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.entity.TSBMessageBody;
import com.tuisongbao.android.engine.chat.serializer.TSBChatMessageBodySerializer;
import com.tuisongbao.android.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.android.engine.chat.serializer.TSBChatMessageTypeSerializer;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;
import com.tuisongbao.android.engine.common.ITSBEngineCallback;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.engineio.EngineConstants;
import com.tuisongbao.android.engine.log.LogUtil;
import com.tuisongbao.android.engine.service.EngineServiceManager;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBChatMessageResponseMessage extends
        BaseTSBResponseMessage<TSBMessage> {
    private TSBMessage mSendMessage;
    /**
     * 用于保存用户传递的call back
     */
    private TSBEngineCallback<TSBMessage> mCustomCallback;

    public TSBChatMessageResponseMessage(TSBChatMessageResponseMessageCallback callback) {
        setCallback(callback);
    }

    public TSBMessage getSendMessage() {
        return mSendMessage;
    }

    public void setSendMessage(TSBMessage sendMessage) {
        this.mSendMessage = sendMessage;
    }

    public TSBEngineCallback<TSBMessage> getCustomCallback() {
        return mCustomCallback;
    }

    public void setCustomCallback(TSBEngineCallback<TSBMessage> customCallback) {
        this.mCustomCallback = customCallback;
    }

    public TSBMessage getCallBackData() {
        return prepareCallBackData();
    }

    @Override
    public TSBMessage parse() {
        return null;
    }

    @Override
    protected TSBMessage prepareCallBackData() {
        TSBMessage message = null;
        TSBConversationDataSource dataSource = new TSBConversationDataSource(TSBEngine.getContext());
        String userId = TSBChatManager.getInstance().getChatUser().getUserId();
        dataSource.open();

        // New Message received.
        if (EngineConstants.CHAT_NAME_NEW_MESSAGE.equals(getBindName())) {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(ChatType.class,
                    new TSBChatMessageChatTypeSerializer());
            gsonBuilder.registerTypeAdapter(TSBMessage.TYPE.class,
                    new TSBChatMessageTypeSerializer());
            gsonBuilder.registerTypeAdapter(TSBMessageBody.class,
                    new TSBChatMessageBodySerializer());
            Gson gson = gsonBuilder.create();
            message = gson.fromJson(getData(),
                    TSBMessage.class);
            EngineServiceManager.receivedMessage(message);
        }

        // Send message successfully
        if (TSBChatMessageSendMessage.NAME.equals(getBindName())) {
            message = getSendMessage();
            JSONObject json;
            try {
                json = new JSONObject(getData());
                long messageId = json.optLong("messageId");
                message.setMessageId(messageId);

                // image messages
                JSONObject content = json.optJSONObject("content");
                if (content != null) {
                    JSONObject file = content.getJSONObject("file");
                    TSBMediaMessageBody body = (TSBMediaMessageBody)message.getBody();
                    body.setDownloadUrl(file.getString("downloadUrl"));

                    message.setBody(body);
                }

                message.setFrom(userId);
                // Keep message order. the server will also create one, after getMessage called, update this field to keep consistent with server.
                message.setCreatedAt(StrUtil.getTimeStringIOS8061(new Date()));
            } catch (JSONException e) {
                LogUtil.error(LogUtil.LOG_TAG_CHAT, e);
            }
        }

        if (message != null && TSBChatManager.getInstance().isCacheEnabled()) {
            dataSource.upsertMessage(userId, message);
        }
        dataSource.close();

        return message;
    }

    @Override
    public void callBack() {
        ((TSBChatMessageResponseMessageCallback)getCallback()).onEvent(this);
    }

    public static interface TSBChatMessageResponseMessageCallback extends ITSBEngineCallback {
        public void onEvent(TSBChatMessageResponseMessage response);
    }
}
