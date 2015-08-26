package com.tuisongbao.engine.chat;

import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.db.ChatConversationDataSource;
import com.tuisongbao.engine.chat.message.ChatMessage;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.entity.ResponseEvent;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.BaseEventHandler;

class ChatMessageNewEventHandler extends BaseEventHandler<ChatMessage> {
    private final String TAG = "TSB" + ChatMessageNewEventHandler.class.getSimpleName();

    public ChatMessageNewEventHandler(Engine engine) {
        setEngine(engine);
    }

    @Override
    public ChatMessage genCallbackData(BaseEvent request, RawEvent response) {
        ChatMessage message = ChatMessage.deserialize(engine, response.getData().toString());
        return message;
    }

    @Override
    protected ChatMessage genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        ChatMessage message = genCallbackData(request, response);
        ChatUser user = engine.getChatManager().getChatUser();

        ChatConversationDataSource dataSource = new ChatConversationDataSource(Engine.getContext(), engine);
        dataSource.open();
        dataSource.upsertMessage(user.getUserId(), message);
        dataSource.close();

        return message;
    }

    @Override
    public void onResponse(BaseEvent request, RawEvent response) {
        sendResponseEvent(response);

        ChatMessage message;
        if (engine.getChatManager().isCacheEnabled()) {
            message = genCallbackDataWithCache(request, response);
        } else {
            message = genCallbackData(request, response);
        }
        engine.getChatManager().trigger(ChatManager.EVENT_MESSAGE_NEW, message);
    }

    private void sendResponseEvent(RawEvent response) {
        ResponseEvent event = new ResponseEvent();
        ResponseEventData data = new ResponseEventData();
        data.setOk(true);
        data.setTo(response.getId());
        event.setData(data);

        engine.getConnection().send(event);
    }
}
