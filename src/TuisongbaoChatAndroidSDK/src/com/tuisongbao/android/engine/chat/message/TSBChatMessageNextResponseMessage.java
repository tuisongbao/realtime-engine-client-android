package com.tuisongbao.android.engine.chat.message;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.entity.ChatType;
import com.tuisongbao.android.engine.chat.entity.TSBChatMessageSendData;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.entity.TSBMessageBody;
import com.tuisongbao.android.engine.chat.serializer.TSBChatMessageBodySerializer;
import com.tuisongbao.android.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;
import com.tuisongbao.android.engine.common.TSBEngineCallback;
import com.tuisongbao.android.engine.log.LogUtil;

public class TSBChatMessageNextResponseMessage extends BaseTSBResponseMessage<String> {

    @Override
    public void callBack() {

        UploadManager manager = new UploadManager();
        String token = TSBChatManager.getInstance().getChatUser().getUploadToken();
        LogUtil.info(LogUtil.LOG_TAG_CHAT, "uploadToken:" + token);

        final TSBChatMessageSendData requestData = parseRequestData();
        String filePath = requestData.getContent().getText();

        Map<String, String> params = new HashMap<String, String>();
        params.put("x:targetId", requestData.getTo());
        params.put("x:messageId", parse());
        final UploadOptions opt = new UploadOptions(params, null, true, null, null);
        manager.put(filePath, null, token, new UpCompletionHandler() {

            @Override
            public void complete(String key, ResponseInfo info, JSONObject responseObject) {
                LogUtil.info(LogUtil.LOG_TAG_CHAT, "Upload file finished with key: " + key + ", info: " + info);
                String userId = TSBChatManager.getInstance().getChatUser().getUserId();
                TSBMessage message = new TSBMessage();
                message.setFrom(userId);
                message.setBody(requestData.getContent());
                message.setRecipient(requestData.getTo());
                message.setChatType(requestData.getType());
                TSBChatManager.getInstance().sendMessage(message, (TSBEngineCallback<TSBMessage>)getCallback());
            }
        }, opt);
    }

    @Override
    public String parse() {
        JSONObject messageIdObject = new JSONObject();
        try {
            messageIdObject = new JSONObject(getData());
        } catch (JSONException e) {
            LogUtil.error(LogUtil.LOG_TAG_CHAT, e);
        }
        return messageIdObject.optString("messageId");
    }

    private TSBChatMessageSendData parseRequestData() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class,
                new TSBChatMessageChatTypeSerializer());
        gsonBuilder.registerTypeAdapter(TSBMessageBody.class,
                new TSBChatMessageBodySerializer());
        Gson gson = gsonBuilder.create();
        String requestDataString = (String)getRequestData();
        return gson.fromJson(requestDataString, TSBChatMessageSendData.class);
    }

}
