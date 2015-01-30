package com.tuisongbao.android.engine.service;

import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.log.LogUtil;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class TSBChatIntentService extends IntentService {
    protected final static String INTENT_ACTION_SEND_MESSAGE_SUCCESS = "com.tuisongbao.android.engine.service.TSBChatIntentService.INTENT_ACTION_SEND_MESSAGE_SUCCESS";
    protected final static String INTENT_ACTION_SEND_MESSAGE_FAILED = "com.tuisongbao.android.engine.service.TSBChatIntentService.INTENT_ACTION_SEND_MESSAGE_FAILED";
    protected final static String INTENT_ACTION_RECEIVED_MESSAGE = "com.tuisongbao.android.engine.service.TSBChatIntentService.INTENT_ACTION_RECEIVED_MESSAGE";
    protected final static String INTENT_EXTRA_KEY_MESSAGE = "com.tuisongbao.android.engine.service.TSBChatIntentService.INTENT_EXTRA_KEY_MESSAGE";
    protected final static String INTENT_EXTRA_KEY_ERROR = "com.tuisongbao.android.engine.service.TSBChatIntentService.INTENT_EXTRA_KEY_ERROR";
    private static final String TAG = TSBChatIntentService.class
            .getSimpleName();

    public TSBChatIntentService() {
        this(TAG);
    }

    public TSBChatIntentService(String name) {
        super(name);
    }

    /**
     * 消息发送成功后接受到的事件
     * 
     * @param context
     * @param msg
     */
    public void onMessageSent(Context context, TSBMessage msg) {
        // 这个时间是来自服务器端的时间，这样即便是多台设备中间也不会出现时间的混乱
        LogUtil.debug(LogUtil.LOG_TAG_SERVICE, "消息发送成功了，发送成功时间是 " + msg.getCreatedAt());
    }

    /**
     * 消息发送失败后接收到的事件
     * 
     * @param context
     * @param msg
     * @param error 发送失败的具体信息，是json格式，e.g. {"code":-9004, "message":"network is unavailable"}
     */
    public void onMessageFailure(Context context, TSBMessage msg, String error) {
        // 这个时间是来自服务器端的时间，这样即便是多台设备中间也不会出现时间的混乱
        LogUtil.debug(LogUtil.LOG_TAG_SERVICE, "消息发送失败：" + error);
    }

    /**
     * 服务端收到消息后接收到的事件
     * 
     * @param context
     * @param msg
     */
    public void onMessage(Context context, TSBMessage msg) {
        // 这个时间是来自服务器端的时间，这样即便是多台设备中间也不会出现时间的混乱
        LogUtil.debug(LogUtil.LOG_TAG_SERVICE, "收到新消息" + msg.getCreatedAt());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        handleMessages(intent);
    }

    private void handleMessages(Intent intent) {
        if (INTENT_ACTION_SEND_MESSAGE_SUCCESS.equals(intent.getAction())) {
            TSBMessage msg = intent.getParcelableExtra(INTENT_EXTRA_KEY_MESSAGE);
            onMessageSent(this, msg);
        } else if (INTENT_ACTION_SEND_MESSAGE_FAILED.equals(intent.getAction())) {
            TSBMessage msg = intent.getParcelableExtra(INTENT_EXTRA_KEY_MESSAGE);
            String error = intent.getParcelableExtra(INTENT_EXTRA_KEY_ERROR);
            onMessageFailure(this, msg, error);
        } else if (INTENT_ACTION_RECEIVED_MESSAGE.equals(intent.getAction())) {
            TSBMessage msg = intent.getParcelableExtra(INTENT_EXTRA_KEY_MESSAGE);
            onMessage(this, msg);
        }
    }

}
