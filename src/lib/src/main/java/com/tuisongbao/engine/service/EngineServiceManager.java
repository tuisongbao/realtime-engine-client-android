package com.tuisongbao.engine.service;

import android.content.Intent;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.entity.TSBMessage;

public class EngineServiceManager {

    public static void receivedMessage(final TSBMessage message) {
        Intent intent = new Intent(TSBEngine.getContext(), getChatIntentService());
        intent.setAction(TSBChatIntentService.INTENT_ACTION_RECEIVED_MESSAGE);
        intent.putExtra(TSBChatIntentService.INTENT_EXTRA_KEY_MESSAGE, message);
        TSBEngine.getContext().startService(intent);
    }

    private static final Class<? extends TSBChatIntentService> getChatIntentService() {
        Class<? extends TSBChatIntentService> chatIntentService = TSBEngine
                .getTSBEngineOptions().getChatIntentService();
        if (chatIntentService == null) {
            chatIntentService = TSBChatIntentService.class;
        }
        return chatIntentService;
    }
}
