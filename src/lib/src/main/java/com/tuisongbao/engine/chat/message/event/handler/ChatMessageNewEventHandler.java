package com.tuisongbao.engine.chat.message.event.handler;

import com.tuisongbao.engine.TSBEngine;
import com.tuisongbao.engine.chat.db.TSBConversationDataSource;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.user.entity.ChatUser;
import com.tuisongbao.engine.common.entity.Event;
import com.tuisongbao.engine.common.entity.ResponseEventData;
import com.tuisongbao.engine.common.event.handler.BaseEventHandler;
import com.tuisongbao.engine.log.LogUtil;

/**
 * Created by root on 15-8-2.
 */
public class ChatMessageNewEventHandler extends BaseEventHandler<ChatMessage> {
    private final String TAG = ChatMessageNewEventHandler.class.getSimpleName();
    private long mRequestId;

    @Override
    public ChatMessage parse(ResponseEventData response) {
        ChatMessage message = ChatMessage.getSerializer().fromJson(response.getResult(), ChatMessage.class);

        // FIXME: 15-8-2
        ResponseEventData data = new ResponseEventData();
        data.setOk(true);
        data.setTo(mRequestId);

        ChatMessage msg = new ChatMessage(mEngine);
        msg.setMessageId(message.getMessageId());
        data.setResult(ChatMessage.getSerializer().toJsonTree(msg));
        try {
            mEngine.connection.send("engine_response", data.toString());
        } catch (Exception e) {
            LogUtil.error(TAG, "Failed to send response message to server");
        }
        return message;
    }

    @Override
    protected ChatMessage prepareCallbackData(Event request, ResponseEventData response) {
        ChatMessage message = parse(response);
        ChatUser user = mEngine.chatManager.getChatUser();
        message.setRecipient(user.getUserId());

        if (message != null && mEngine.chatManager.isCacheEnabled()) {
            TSBConversationDataSource dataSource = new TSBConversationDataSource(TSBEngine.getContext(), mEngine);
            dataSource.open();
            dataSource.upsertMessage(user.getUserId(), message);
            dataSource.close();
        }

        return message;
    }

    @Override
    public void callback(Event request, Event response) {
        mRequestId = response.getId();
        super.callback(request, response);
    }
}
