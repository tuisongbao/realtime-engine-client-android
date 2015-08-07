package com.tuisongbao.engine.demo.chat.service;

import android.content.Context;
import android.content.Intent;

import com.tuisongbao.engine.chat.ChatIntentService;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;

public class ChatMessageRevieveService extends ChatIntentService {

    public final static String BROADCAST_ACTION_RECEIVED_MESSAGE = "com.tuisongbao.android.engine.demo.chat.service.ChatMessageRevieveService.BROADCAST_ACTION_RECEIVED_MESSAGE";
    public final static String BROADCAST_EXTRA_KEY_MESSAGE = "com.tuisongbao.android.engine.demo.chat.service.ChatMessageRevieveService.BROADCAST_EXTRA_KEY_MESSAGE";

    @Override
    public void onMessage(Context context, ChatMessage msg) {
        super.onMessage(context, msg);
        Intent intent = new Intent(BROADCAST_ACTION_RECEIVED_MESSAGE);
        intent.putExtra(BROADCAST_EXTRA_KEY_MESSAGE, msg.serialize());
        sendBroadcast(intent);
    }
}
