package com.tuisongbao.android.engine.chat.entity;

public class TSBChatMessageSendData {
    private ChatType type;
    private String to;
    private TSBMessageBody content;

    public ChatType getType() {
        return type;
    }

    public void setType(ChatType type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public TSBMessageBody getContent() {
        return content;
    }

    public void setContent(TSBMessageBody content) {
        this.content = content;
    }
}
