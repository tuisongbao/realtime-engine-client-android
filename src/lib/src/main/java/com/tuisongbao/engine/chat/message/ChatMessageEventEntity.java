package com.tuisongbao.engine.chat.message;

public class ChatMessageEventEntity {
    private ChatMessageEventContent.TYPE type;
    private String target;

    public String getTarget() {
        return target;
    }

    public ChatMessageEventContent.TYPE getType() {
        return type;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setType(ChatMessageEventContent.TYPE type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("ChatMessageContent[type:%s, file:%s]", type, target);
    }
}
