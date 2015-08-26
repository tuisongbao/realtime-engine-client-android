package com.tuisongbao.engine.chat.conversation;

import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.db.ChatConversationDataSource;
import com.tuisongbao.engine.chat.ChatUser;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.BaseEventHandler;

class ChatConversationDeleteEventHandler extends BaseEventHandler<String> {
    @Override
    protected String genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        ChatUser chatUser = engine.getChatManager().getChatUser();
        ChatConversationDataSource conversationDataSource = new ChatConversationDataSource(Engine.getContext(), engine);
        ChatConversation conversation = ((ChatConversationDeleteEvent)request).getData();

        conversationDataSource.open();
        conversationDataSource.remove(chatUser.getUserId(), conversation.getType(), conversation.getTarget());
        conversationDataSource.close();

        return "OK";
    }

    @Override
    protected String genCallbackData(BaseEvent request, RawEvent response) {
        return "OK";
    }
}
