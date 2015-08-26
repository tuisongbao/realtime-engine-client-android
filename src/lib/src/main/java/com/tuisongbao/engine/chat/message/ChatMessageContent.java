package com.tuisongbao.engine.chat.message;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tuisongbao.engine.Engine;

import org.json.JSONObject;

/**
 * 消息主体内容
 *
 */
public class ChatMessageContent {
    transient private final String TAG = "TSB" + ChatMessage.class.getSimpleName();

    protected ChatMessage.TYPE type;
    protected String text;
    protected ChatMessageFileEntity file;
    protected ChatMessageEventEntity event;
    protected ChatMessageLocationEntity location;
    protected JsonObject extra;

    transient protected Engine mEngine;

    public ChatMessageContent() {
        type = ChatMessage.TYPE.TEXT;
    }

    public static ChatMessageContent getConcreteContent(ChatMessage.TYPE type) {
        ChatMessageContent content = new ChatMessageContent();
        if (type == ChatMessage.TYPE.IMAGE) {
            content = new ChatMessageImageContent();
        } else if (type == ChatMessage.TYPE.VOICE) {
            content = new ChatMessageVoiceContent();
        } else if (type == ChatMessage.TYPE.VIDEO) {
            content = new ChatMessageVideoContent();
        } else if (type == ChatMessage.TYPE.LOCATION) {
            content = new ChatMessageLocationContent();
        } else if (type == ChatMessage.TYPE.EVENT) {
            content = new ChatMessageEventContent();
        }
        return content;
    }

    public void setEngine(Engine engine) {
        mEngine = engine;
    }

    public ChatMessage.TYPE getType() {
        return type;
    }

    public void setType(ChatMessage.TYPE type) {
        this.type = type;
    }

    public void setFile(ChatMessageFileEntity file) {
        this.file = file;
    }

    public ChatMessageFileEntity getFile() {
        return file;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setExtra(JSONObject extra) {
        this.extra = new Gson().fromJson(extra.toString(), JsonObject.class);
    }

    public void setExtra(JsonObject extra) {
        this.extra = extra;
    }

    public JsonObject getExtra() {
        return extra;
    }

    public ChatMessageEventEntity getEvent() {
        return event;
    }

    public void setEvent(ChatMessageEventEntity event) {
        this.event = event;
    }

    public ChatMessageLocationEntity getLocation() {
        return location;
    }

    public void setLocation(ChatMessageLocationEntity location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return String.format("ChatMessageContent[type:%s, file:%s, event: %s, location:%s, extra: %s]", type, getFile()
                , event, location, extra);
    }

    public  boolean isMediaMessage() {
        ChatMessage.TYPE contentType = getType();
        return contentType == ChatMessage.TYPE.IMAGE || contentType == ChatMessage.TYPE.VOICE || contentType == ChatMessage.TYPE.VIDEO;
    }
}
