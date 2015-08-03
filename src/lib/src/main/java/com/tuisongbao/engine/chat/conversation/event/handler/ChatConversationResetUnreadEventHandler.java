package com.tuisongbao.engine.chat.conversation.event.handler;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
import com.tuisongbao.engine.chat.conversation.event.ChatConversationDeleteEvent;
import com.tuisongbao.engine.chat.db.ChatConversationDataSource;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;

/**
 * Created by root on 15-8-3.
 */
public class ChatConversationResetUnreadEventHandler extends BaseEventHandler<String> {
    @Override
    protected String genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        ChatUser chatUser = engine.getChatManager().getChatUser();
        ChatConversationDataSource conversationDataSource = new ChatConversationDataSource(TSBEngine.getContext(), engine);
        ChatConversation conversation = ((ChatConversationDeleteEvent)request).getData();

        conversationDataSource.open();
        conversationDataSource.resetUnread(chatUser.getUserId(), conversation.getType(), conversation.getTarget());
        conversationDataSource.close();

        return "OK";
    }

    @Override
    protected String genCallbackData(BaseEvent request, RawEvent response) {
        return "OK";
    }
}
