package com.tuisongbao.engine.chat.message.entity;

import com.google.gson.JsonObject;
import com.tuisongbao.engine.chat.user.ChatType;

public class ChatMessageSendData {
    private ChatType type;
    private String to;
    private ChatMessageBody content;
    /**
     * The extra of JSONObject type would create 'nameValuePairs' field after serialization by gson.
     */
    private JsonObject extra;

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

    public ChatMessageBody getContent() {
        return content;
    }

    public void setContent(ChatMessageBody content) {
        this.content = content;
    }

    public void setExtra(JsonObject extra) {
        this.extra = extra;
    }

    public JsonObject getExtra() {
        return extra;
    }
}
