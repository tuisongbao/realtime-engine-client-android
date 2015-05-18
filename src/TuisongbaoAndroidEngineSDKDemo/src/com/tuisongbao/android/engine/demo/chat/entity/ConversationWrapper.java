package com.tuisongbao.android.engine.demo.chat.entity;

import java.util.List;

import com.tuisongbao.android.engine.chat.entity.TSBChatConversation;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.common.TSBEngineCallback;

public class ConversationWrapper {
    private TSBChatConversation conversation;
    private TSBMessage latestMessage;

    public TSBChatConversation getConversation() {
        return conversation;
    }

    public void setConversation(TSBChatConversation conversation) {
        this.conversation = conversation;
    }

    public void setLatestMessage(TSBMessage latestMessage) {
        this.latestMessage = latestMessage;
    }

    public TSBMessage getLatestMessage() {
        return latestMessage;
    }

    public void loadLatestMessage(final TSBEngineCallback<String> callback) {
        if (latestMessage != null) {
            return;
        }
        conversation.getMessages(null, null, 1, new TSBEngineCallback<List<TSBMessage>>() {

            @Override
            public void onSuccess(List<TSBMessage> t) {
                if (t != null && t.size() > 0) {
                    latestMessage = t.get(0);
                }
                if (callback != null) {
                    callback.onSuccess("OK");
                }
            }

            @Override
            public void onError(int code, String message) {

            }
        });
    }
}
