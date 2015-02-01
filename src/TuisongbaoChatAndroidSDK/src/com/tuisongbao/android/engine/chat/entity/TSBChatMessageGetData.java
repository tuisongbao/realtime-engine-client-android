package com.tuisongbao.android.engine.chat.entity;

public class TSBChatMessageGetData {
    private ChatType type;
    private String target;
    private Long startMessageId;
    private Long endMessageId;
    private int limit;

    public ChatType getType() {
        return type;
    }

    public void setType(ChatType type) {
        this.type = type;
    }

    public Long getStartMessageId() {
        return startMessageId;
    }

    public void setStartMessageId(Long startMessageId) {
        this.startMessageId = startMessageId;
    }

    public Long getEndMessageId() {
        return endMessageId;
    }

    public void setEndMessageId(Long endMessageId) {
        this.endMessageId = endMessageId;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
