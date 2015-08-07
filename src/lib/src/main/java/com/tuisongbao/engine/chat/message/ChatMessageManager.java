package com.tuisongbao.engine.chat.message;

import android.util.Log;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.ChatOptions;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageContent;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageFileContent;
import com.tuisongbao.engine.chat.message.event.ChatMessageSendEvent;
import com.tuisongbao.engine.chat.message.event.handler.ChatMessageSendEventHandler;
import com.tuisongbao.engine.common.BaseManager;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.log.LogUtil;
import com.tuisongbao.engine.utils.StrUtils;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by root on 15-8-3.
 */
public class ChatMessageManager extends BaseManager {
    private static final String TAG = "TSB" + ChatMessageManager.class.getSimpleName();

    public ChatMessageManager(Engine engine) {
        super(engine);
    }

    /**
     * 发送消息
     *
     * @param message
     *            消息
     * @param callback
     */
    public ChatMessage sendMessage(final ChatMessage message,
                            final EngineCallback<ChatMessage> callback, ChatOptions options) {
        try {
            message.setEngine(engine);
            ChatMessage.TYPE messageType = message.getContent().getType();
            if (messageType == ChatMessage.TYPE.TEXT) {
                sendMessageEvent(message, callback);
            } else {
                sendMediaMessage(message, callback, options);
            }
        } catch (Exception e) {
            callback.onError(engine.getUnhandledResponseError());
            LogUtil.error(TAG, e);
        }
        return message;
    }

    private void sendMessageEvent(ChatMessage message, EngineCallback<ChatMessage> callback)  {
        ChatMessageSendEvent event = new ChatMessageSendEvent();
        message.setCreatedAt(StrUtils.getTimeStringIOS8061(new Date()));
        event.setData(message);
        ChatMessageSendEventHandler handler = new ChatMessageSendEventHandler();
        handler.setCallback(callback);

        send(event, handler);
    }

    private void sendMediaMessage(final ChatMessage message, final EngineCallback<ChatMessage> callback, ChatOptions options) {
        EngineCallback<JSONObject> handlerCallback = getUploaderHandlerOfMediaMessage(message, callback);
        if (!uploadMessageResourceToQiniu(message, handlerCallback, options)) {
            ResponseError error = new ResponseError();
            error.setMessage("Can not find the source you specified.");
            callback.onError(error);
        }
    }

    private boolean uploadMessageResourceToQiniu(ChatMessage message, final EngineCallback<JSONObject> responseHandler,
                                                 final ChatOptions options) {
        ChatMessageContent content = message.getContent();
        String filePath = content.getFile().getFilePath();
        if (StrUtils.isEmpty(filePath)) {
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

    private EngineCallback<JSONObject> getUploaderHandlerOfMediaMessage(final ChatMessage message,
                                                                           final EngineCallback<ChatMessage> callback) {
        EngineCallback<JSONObject> responseHandler = new EngineCallback<JSONObject>() {

            @Override
            public void onSuccess(JSONObject responseObject) {
                ChatMessageContent content = message.getContent();
                ChatMessageFileContent file = new ChatMessageFileContent();
                try {
                    file.setKey(responseObject.getString("key"));
                } catch (Exception e) {
                    LogUtil.error(TAG, e);

                    // If can not get key from response, Server can not generate download url, so call error directly.
                    ResponseError error = new ResponseError();
                    error.setMessage("Failed to upload resource");
                    callback.onError(error);
                    return;
                }
                try {
                    LogUtil.info(TAG, "Get response from QINIU " + responseObject.toString(4));

                    ChatMessage.TYPE messageType = content.getType();
                    if (messageType == ChatMessage.TYPE.IMAGE) {
                        JSONObject imageInfoInResponse = responseObject.getJSONObject("imageInfo");
                        file.setFrame(imageInfoInResponse.getInt("width"), imageInfoInResponse.getInt("height"));
                    } else if (messageType == ChatMessage.TYPE.VOICE || messageType == ChatMessage.TYPE.VIDEO) {
                        JSONObject formatInfoInResponse = responseObject.getJSONObject("avinfo").getJSONObject("format");
                        file.setDuration(formatInfoInResponse.getDouble("duration"));
                    }
                } catch (Exception e) {
                    LogUtil.error(TAG, e);
                } finally {
                    // If some exception occurs when parsing the other properties, do not block sending message.
                    content.setFile(file);
                    message.setContent(content);
                    sendMessageEvent(message, callback);
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
