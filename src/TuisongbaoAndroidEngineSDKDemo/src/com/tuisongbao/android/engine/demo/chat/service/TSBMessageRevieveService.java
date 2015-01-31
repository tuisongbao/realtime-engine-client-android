package com.tuisongbao.android.engine.demo.chat.service;

import android.content.Context;
import android.content.Intent;

import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.service.TSBChatIntentService;

public class TSBMessageRevieveService extends TSBChatIntentService {

    public final static String BROADCAST_ACTION_RECEIVED_MESSAGE = "com.tuisongbao.android.engine.demo.chat.service.TSBMessageRevieveService.BROADCAST_ACTION_RECEIVED_MESSAGE";
    public final static String BROADCAST_EXTRA_KEY_MESSAGE = "com.tuisongbao.android.engine.demo.chat.service.TSBMessageRevieveService.BROADCAST_EXTRA_KEY_MESSAGE";

    @Override
    public void onMessage(Context context, TSBMessage msg) {
        super.onMessage(context, msg);
        Intent intent = new Intent(BROADCAST_ACTION_RECEIVED_MESSAGE);
        intent.putExtra(BROADCAST_EXTRA_KEY_MESSAGE, msg);
        sendBroadcast(intent);
    }
}
