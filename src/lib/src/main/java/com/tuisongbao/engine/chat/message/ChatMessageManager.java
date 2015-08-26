package com.tuisongbao.engine.chat.message;

import android.util.Log;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.db.ChatConversationDataSource;
import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.common.BaseManager;
import com.tuisongbao.engine.common.callback.EngineCallback;
import com.tuisongbao.engine.common.callback.ProgressCallback;
import com.tuisongbao.engine.common.entity.ResponseError;
import com.tuisongbao.engine.utils.LogUtils;
import com.tuisongbao.engine.utils.StrUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatMessageManager extends BaseManager {
    private static final String TAG = "TSB" + ChatMessageManager.class.getSimpleName();
    private ChatConversationDataSource dataSource;

    public ChatMessageManager(Engine engine) {
        super(engine);
        if (engine.getChatManager().isCacheEnabled()) {
            dataSource = new ChatConversationDataSource(Engine.getContext(), engine);
        }
    }

    /**
     * 发送消息
     *
     * @param message           消息
     * @param callback          结果处理方法
     * @param progressCallback  进度处理方法
     */
    public ChatMessage sendMessage(final ChatMessage message,
                            final EngineCallback<ChatMessage> callback, ProgressCallback progressCallback) {
        try {
            ChatMessage.TYPE messageType = message.getContent().getType();
            if (messageType == ChatMessage.TYPE.TEXT || messageType == ChatMessage.TYPE.LOCATION) {
                sendMessageEvent(message, callback);
            } else {
                sendMediaMessage(message, callback, progressCallback);
            }
        } catch (Exception e) {
            callback.onError(engine.getUnhandledResponseError());
            LogUtils.error(TAG, e);
        }
        return message;
    }

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
            LogUtils.error(TAG, e);
        }
    }

    private void requestMissingMessagesInLocalCache(ChatType chatType, String target, Long startMessageId,
                                                    Long endMessageId, int limit, EngineCallback<List<ChatMessage>> callback) {
        ChatMessageMultiGetEventHandler response = new ChatMessageMultiGetEventHandler();
        response.setMessageIdSpan(startMessageId, endMessageId);
        response.setCallback(callback);

        String currentUserId = engine.getChatManager().getChatUser().getUserId();
        // Query local data
        dataSource.open();
        List<ChatMessage> messages = dataSource.getMessages(currentUserId, chatType, target, startMessageId, endMessageId, limit);
        LogUtils.debug(TAG, "Get " + messages.size() + " messages");
        dataSource.close();

        // if startMessageId is null, pull the latest messages.
        if (messages.size() < 1 || startMessageId == null) {
            ChatMessageGetEvent message = getRequestOfGetMessages(chatType, target, startMessageId, endMessageId, limit);
            response.incRequestCount();
            send(message, response);
            return;
        }

        // Check whether missing messages from beginning.
        Long maxCachedMessageId = messages.get(0).getMessageId();
        if (maxCachedMessageId < startMessageId) {
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

    private void sendMessageEvent(ChatMessage message, EngineCallback<ChatMessage> callback)  {
        ChatMessageSendEvent event = new ChatMessageSendEvent();
        message.setCreatedAt(StrUtils.getTimeStringIOS8061(new Date()));
        event.setData(message);
        ChatMessageSendEventHandler handler = new ChatMessageSendEventHandler();
        handler.setCallback(callback);

        send(event, handler);
    }

    private void sendMediaMessage(final ChatMessage message, final EngineCallback<ChatMessage> callback,
                                  ProgressCallback progressCallback) {
        EngineCallback<JSONObject> handlerCallback = getUploaderHandlerOfMediaMessage(message, callback);
        if (!uploadMessageResourceToQiniu(message, handlerCallback, progressCallback)) {
            ResponseError error = new ResponseError();
            error.setMessage("Can not find the source you specified.");
            callback.onError(error);
        }
    }

    private boolean uploadMessageResourceToQiniu(ChatMessage message, final EngineCallback<JSONObject> responseHandler,
                                                 final ProgressCallback progressCallback) {
        ChatMessageContent content = message.getContent();
        String filePath = content.getFile().getFilePath();
        if (StrUtils.isEmpty(filePath)) {
            return false;
        }

        UploadManager manager = new UploadManager();
        String token = engine.getChatManager().getChatUser().getUploadToken();
        Log.d(TAG, token);
        UpProgressHandler progressHandler = null;
        if (progressCallback != null) {
            progressHandler = new UpProgressHandler() {

                @Override
                public void progress(String arg0, double percent) {
                    progressCallback.progress((int)(percent * 100));
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
        return new EngineCallback<JSONObject>() {

            @Override
            public void onSuccess(JSONObject responseObject) {
                ChatMessageContent content = message.getContent();
                ChatMessageFileEntity file = content.getFile();
                try {
                    file.setKey(responseObject.getString("key"));
                } catch (Exception e) {
                    LogUtils.error(TAG, e);

                    // If can not get key from response, Server can not generate download url, so call error directly.
                    ResponseError error = new ResponseError();
                    error.setMessage("Failed to upload resource");
                    callback.onError(error);
                    return;
                }
                try {
                    LogUtils.info(TAG, "Get response from QINIU " + responseObject.toString(4));

                    ChatMessage.TYPE messageType = content.getType();
                    if (messageType == ChatMessage.TYPE.IMAGE) {
                        JSONObject imageInfoInResponse = responseObject.getJSONObject("imageInfo");
                        file.setFrame(imageInfoInResponse.getInt("width"), imageInfoInResponse.getInt("height"));
                    } else if (messageType == ChatMessage.TYPE.VOICE || messageType == ChatMessage.TYPE.VIDEO) {
                        JSONObject formatInfoInResponse = responseObject.getJSONObject("avinfo").getJSONObject("format");
                        file.setDuration(formatInfoInResponse.getDouble("duration"));
                    }
                    file.setMimeType(responseObject.getString("mimeType"));
                    file.setEtag(responseObject.getString("etag"));
                    file.setSize(responseObject.getDouble("fsize"));
                } catch (Exception e) {
                    LogUtils.error(TAG, e);
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
    }
}
