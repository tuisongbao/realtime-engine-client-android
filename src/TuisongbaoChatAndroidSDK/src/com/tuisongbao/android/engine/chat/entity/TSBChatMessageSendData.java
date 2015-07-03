package com.tuisongbao.android.engine.chat.entity;

import com.google.gson.JsonObject;

public class TSBChatMessageSendData {
    private ChatType type;
    private String to;
    private TSBMessageBody content;
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

    public TSBMessageBody getContent() {
        return content;
    }

    public void setContent(TSBMessageBody content) {
        this.content = content;
    }

    public void setExtra(JsonObject extra) {
        this.extra = extra;
    }

    public JsonObject getExtra() {
        return extra;
    }
}
