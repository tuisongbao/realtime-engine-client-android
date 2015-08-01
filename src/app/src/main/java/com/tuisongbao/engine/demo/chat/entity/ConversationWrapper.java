package com.tuisongbao.engine.demo.chat.entity;

import java.util.List;

import com.tuisongbao.engine.chat.conversation.entity.ChatConversation;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.common.callback.TSBEngineCallback;

public class ConversationWrapper {
    public int localUnreadCount = 0;

    private ChatConversation conversation;
    private ChatMessage latestMessage;

    public ChatConversation getConversation() {
        return conversation;
    }

    public void setConversation(ChatConversation conversation) {
        this.conversation = conversation;
    }

    public void setLatestMessage(ChatMessage latestMessage) {
        this.latestMessage = latestMessage;
    }

    public ChatMessage getLatestMessage() {
        return latestMessage;
    }

    public void loadLatestMessage(final TSBEngineCallback<String> callback) {
        if (latestMessage != null) {
            return;
        }
        conversation.getMessages(null, null, 1, new TSBEngineCallback<List<ChatMessage>>() {

            @Override
            public void onSuccess(List<ChatMessage> t) {
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
