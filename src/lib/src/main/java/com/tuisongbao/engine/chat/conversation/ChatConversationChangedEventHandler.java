package com.tuisongbao.engine.chat.conversation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.engine.Engine;
import com.tuisongbao.engine.chat.ChatType;
import com.tuisongbao.engine.chat.db.ChatConversationDataSource;
import com.tuisongbao.engine.chat.serializer.ChatTypeSerializer;
import com.tuisongbao.engine.common.Protocol;
import com.tuisongbao.engine.common.entity.RawEvent;
import com.tuisongbao.engine.common.event.BaseEvent;
import com.tuisongbao.engine.common.event.BaseEventHandler;

/***
 *
 */
public class ChatConversationChangedEventHandler extends BaseEventHandler<ChatConversation> {
    private final String TAG = "TSB" + ChatConversationChangedEventHandler.class.getSimpleName();

    public ChatConversationChangedEventHandler(Engine engine) {
        setEngine(engine);
    }

    @Override
    protected ChatConversation genCallbackDataWithCache(BaseEvent request, RawEvent response) {
        ChatConversation changedConversation = genCallbackData(request, response);

        ChatConversationDataSource dataSource = new ChatConversationDataSource(Engine.getContext(), engine);
        String userId = engine.getChatManager().getChatUser().getUserId();
        dataSource.open();
        dataSource.upsert(changedConversation, userId);
        dataSource.close();

        return changedConversation;
    }

    @Override
    protected ChatConversation genCallbackData(BaseEvent request, RawEvent response) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class, new ChatTypeSerializer());
        Gson gson = gsonBuilder.create();
        return gson.fromJson(response.getData().toString(), ChatConversation.class);
    }

    @Override
    public void onResponse(BaseEvent request, RawEvent response) {
        ChatConversation changedConversation = genCallbackDataWithCache(request, response);
        engine.getChatManager().getConversationManager().trigger(Protocol.EVENT_NAME_CONVERSATION_CHANGED, changedConversation);
    }
}
