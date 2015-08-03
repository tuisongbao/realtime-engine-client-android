package com.tuisongbao.engine.chat.message.event.handler;

import android.content.Intent;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.db.TSBConversationDataSource;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseEvent;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;
import com.tuisongbao.engine.service.ChatIntentService;

/**
 * Created by root on 15-8-2.
 */
public class ChatMessageNewEventHandler extends BaseEventHandler<ChatMessage> {
    private final String TAG = ChatMessageNewEventHandler.class.getSimpleName();
    private long mRequestId;

    @Override
    public ChatMessage genCallbackData(BaseEvent request, RawEvent response) {
        ChatMessage message = ChatMessage.getSerializer().fromJson(response.getData(), ChatMessage.class);
        return message;
    }

    @Override
    protected ChatMessage genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        ChatMessage message = genCallbackData(request, response);
        ChatUser user = mEngine.chatManager.getChatUser();

        TSBConversationDataSource dataSource = new TSBConversationDataSource(TSBEngine.getContext(), mEngine);
        dataSource.open();
        dataSource.upsertMessage(user.getUserId(), message);
        dataSource.close();

        return message;
    }

    @Override
    public void onResponse(BaseEvent request, RawEvent response) {
        sendResponseEvent(response);

        ChatMessage message;
        if (mEngine.chatManager.isCacheEnabled()) {
            message = genCallbackDataWithCache(request, response);
        } else {
            message = genCallbackData(request, response);
        }
        receivedMessage(message);
    }

    private void sendResponseEvent(RawEvent response) {
        ResponseEvent event = new ResponseEvent();
        ResponseEventData data = new ResponseEventData();
        data.setOk(true);
        data.setTo(response.getId());
        event.setData(data);

        mEngine.connection.send(event);
    }

    private void receivedMessage(final ChatMessage message) {
        Intent intent = new Intent(TSBEngine.getContext(), getChatIntentService());
        intent.setAction(ChatIntentService.INTENT_ACTION_RECEIVED_MESSAGE);
        intent.putExtra(ChatIntentService.INTENT_EXTRA_KEY_MESSAGE, message);
        TSBEngine.getContext().startService(intent);
    }

    private final Class<? extends ChatIntentService> getChatIntentService() {
        Class<? extends ChatIntentService> chatIntentService = mEngine.getEngineOptions().getChatIntentService();
        if (chatIntentService == null) {
            chatIntentService = ChatIntentService.class;
        }
        return chatIntentService;
    }
}
