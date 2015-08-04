package com.tuisongbao.engine.chat;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.log.LogUtil;

public class ChatIntentService extends IntentService {
    public final static String INTENT_ACTION_RECEIVED_MESSAGE = "com.tuisongbao.android.engine.service.ChatIntentService.INTENT_ACTION_RECEIVED_MESSAGE";
    public final static String INTENT_EXTRA_KEY_MESSAGE = "com.tuisongbao.android.engine.service.ChatIntentService.INTENT_EXTRA_KEY_MESSAGE";

    private static final String TAG = "TSB" + ChatIntentService.class.getSimpleName();

    public ChatIntentService() {
        this(TAG);
    }

    public ChatIntentService(String name) {
        super(name);
    }

    protected void onMessage(Context context, ChatMessage msg) {
        LogUtil.debug(TAG, "New message: " + msg);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        handleMessages(intent);
    }

    private void handleMessages(Intent intent) {
        if (INTENT_ACTION_RECEIVED_MESSAGE.equals(intent.getAction())) {
            ChatMessage msg = intent.getParcelableExtra(INTENT_EXTRA_KEY_MESSAGE);
            onMessage(this, msg);
        }
    }

}
