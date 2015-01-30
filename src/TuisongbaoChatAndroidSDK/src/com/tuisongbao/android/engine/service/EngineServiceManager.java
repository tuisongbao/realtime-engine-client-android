package com.tuisongbao.android.engine.service;

import android.content.Intent;

import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;

public class EngineServiceManager {

    public static void sendMessageSuccess(TSBMessage message) {
        Intent intent = new Intent(TSBEngine.getContext(), getChatIntentService());
        intent.setAction(TSBChatIntentService.INTENT_ACTION_SEND_MESSAGE_SUCCESS);
        intent.putExtra(TSBChatIntentService.INTENT_EXTRA_KEY_MESSAGE, message);
        TSBEngine.getContext().startService(intent);
    }

    public static void sendMessageFailure(TSBMessage message, String error) {
        Intent intent = new Intent(TSBEngine.getContext(), getChatIntentService());
        intent.setAction(TSBChatIntentService.INTENT_ACTION_SEND_MESSAGE_FAILED);
        intent.putExtra(TSBChatIntentService.INTENT_EXTRA_KEY_MESSAGE, message);
        intent.putExtra(TSBChatIntentService.INTENT_EXTRA_KEY_ERROR, error);
        TSBEngine.getContext().startService(intent);
    }

    public static void receivedMessage(TSBMessage message) {
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
