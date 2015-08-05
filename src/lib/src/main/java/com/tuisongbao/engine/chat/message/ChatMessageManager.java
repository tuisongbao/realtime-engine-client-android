package com.tuisongbao.engine.chat.message;

import android.util.Log;

import com.google.gson.JsonObject;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.ChatOptions;
import com.tuisongbao.engine.chat.message.entity.ChatImageMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatMediaMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatVoiceMessageBody;
import com.tuisongbao.engine.chat.message.event.ChatMessageSendEvent;
import com.tuisongbao.engine.chat.message.event.handler.ChatMessageSendEventHandler;
import com.tuisongbao.engine.common.BaseManager;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.util.StrUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by root on 15-8-3.
 */
public class ChatMessageManager extends BaseManager {
    private static final String TAG = "TSB" + ChatMessageManager.class.getSimpleName();

    public ChatMessageManager(TSBEngine engine) {
        super(engine);
    }

    /**
     * 发送消息
     *
     * @param message
     *            消息
     * @param callback
     */
    public void sendMessage(final ChatMessage message,
                            final TSBEngineCallback<ChatMessage> callback, ChatOptions options) {
        try {
            ChatMessage.TYPE messageType = message.getContent().getType();
            if (messageType == ChatMessage.TYPE.TEXT) {
                sendMessageRequest(message, callback);
            } else {
                sendMediaMessage(message, callback, options);
            }
        } catch (Exception e) {
            callback.onError(engine.getUnhandledResponseError());
            LogUtil.error(TAG, e);
        }
    }

    private void sendMessageRequest(ChatMessage message, TSBEngineCallback<ChatMessage> callback) throws JSONException {
        ChatMessageSendEvent request = new ChatMessageSendEvent();
        message.setCreatedAt(StrUtil.getTimeStringIOS8061(new Date()));
        request.setData(message);
        ChatMessageSendEventHandler handler = new ChatMessageSendEventHandler();
        handler.setCallback(callback);

        send(request, handler);
    }

    private void sendMediaMessage(final ChatMessage message, final TSBEngineCallback<ChatMessage> callback, ChatOptions options) {
        TSBEngineCallback<JSONObject> handlerCallback = getUploaderHandlerOfMediaMessage(message, callback);
        if (!uploadMessageResourceToQiniu(message, handlerCallback, options)) {
            ResponseError error = new ResponseError();
            error.setMessage("Can not find the source you specified.");
            callback.onError(error);
        }
    }

    private boolean uploadMessageResourceToQiniu(ChatMessage message, final TSBEngineCallback<JSONObject> responseHandler,
                                                 final ChatOptions options) {
        ChatMediaMessageBody mediaBody = (ChatMediaMessageBody)message.getContent();
        String filePath = mediaBody.getLocalPath();
        if (StrUtil.isEmpty(filePath)) {
            return false;
        }

        UploadManager manager = new UploadManager();
        String token = engine.getChatManager().getChatUser().getUploadToken();
        Log.d(TAG, token);
        UpProgressHandler progressHandler = null;
        if (options != null) {
            progressHandler = new UpProgressHandler() {

                @Override
                public void progress(String arg0, double percent) {
                    options.callbackProgress((int)(percent * 100));
                }
            };
        }

        Map<String, String> params = new HashMap<>();
        params.put("x:targetId", message.getRecipient());
        final UploadOptions opt = new UploadOptions(params, null, true, progressHandler, null);
        manager.put(filePath, null, token, new UpCompletionHandler() {

            @Override
            public void complete(String key, ResponseInfo info, JSONObject responseObject) {
                Log.i(TAG, "Get response of qiniu, info: " + info.isOK() + " error: " + info.error);
                if (!info.isOK()) {
                    ResponseError error = new ResponseError();
                    error.setMessage("Failed to upload source");
                    responseHandler.onError(error);
                } else {
                    responseHandler.onSuccess(responseObject);
                }
            }
        }, opt);
        return true;
    }

    private TSBEngineCallback<JSONObject> getUploaderHandlerOfMediaMessage(final ChatMessage message,
                                                                           final TSBEngineCallback<ChatMessage> callback) {
        TSBEngineCallback<JSONObject> responseHandler = new TSBEngineCallback<JSONObject>() {

            @Override
            public void onSuccess(JSONObject responseObject) {
                try {
                    LogUtil.info(TAG, "Get response from QINIU " + responseObject.toString(4));
                    ChatMediaMessageBody body = (ChatMediaMessageBody) message.getContent();

                    JsonObject file = new JsonObject();
                    file.addProperty(ChatImageMessageBody.KEY, responseObject.getString("key"));
                    file.addProperty(ChatImageMessageBody.ETAG, responseObject.getString("etag"));
                    file.addProperty(ChatImageMessageBody.NAME, responseObject.getString("fname"));
                    file.addProperty(ChatImageMessageBody.SIZE, responseObject.getString("fsize"));
                    file.addProperty(ChatImageMessageBody.MIME_TYPE, responseObject.getString("mimeType"));

                    ChatMessage.TYPE messageType = body.getType();
                    if (messageType == ChatMessage.TYPE.IMAGE) {
                        JSONObject imageInfoInResponse = responseObject.getJSONObject("imageInfo");
                        file.addProperty(ChatImageMessageBody.IMAGE_INFO_WIDTH, imageInfoInResponse.getInt("width"));
                        file.addProperty(ChatImageMessageBody.IMAGE_INFO_HEIGHT, imageInfoInResponse.getInt("height"));
                        body.setFile(file);
                    } else if (messageType == ChatMessage.TYPE.VOICE || messageType == ChatMessage.TYPE.VIDEO) {
                        JSONObject formatInfoInResponse = responseObject.getJSONObject("avinfo").getJSONObject("format");
                        file.addProperty(ChatVoiceMessageBody.VOICE_INFO_DURATION, formatInfoInResponse.getString("duration"));
                        body.setFile(file);
                    }
                    body.setFile(file);
                    message.setContent(body);
                    sendMessageRequest(message, callback);
                } catch (Exception e) {
                    LogUtil.error(TAG, e);
                }
            }

            @Override
            public void onError(ResponseError error) {
                callback.onError(error);
            }
        };
        return responseHandler;
    }
}
