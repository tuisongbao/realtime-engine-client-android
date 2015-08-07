package com.tuisongbao.engine.chat.message.entity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageEventContent;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageFileContent;

import org.json.JSONObject;

/**
 * Created by root on 15-8-5.
 */
public class ChatMessageContent {
    private ChatMessage.TYPE type;
    private String text;
    private ChatMessageFileContent file;
    private ChatMessageEventContent event;
    private JsonObject extra;

    public ChatMessageContent() {
    }

    public ChatMessage.TYPE getType() {
        return type;
    }

    public void setType(ChatMessage.TYPE type) {
        this.type = type;
    }

    public void setFile(ChatMessageFileContent file) {
        this.file = file;
    }

    public ChatMessageFileContent getFile() {
        if (file == null) {
            file = new ChatMessageFileContent();
        }
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

    public JsonElement getExtra() {
        return extra;
    }

    public ChatMessageEventContent getEvent() {
        if (event == null) {
            event = new ChatMessageEventContent();
        }
        return event;
    }

    public void setEvent(ChatMessageEventContent event) {
        this.event = event;
    }

    public void setFilePath(String path) {
        if (file == null) {
            file = new ChatMessageFileContent();
        }
        file.setFilePath(path);
    }

    @Override
    public String toString() {
        return String.format("ChatMessageContent[url:%s, thumbUrl: %s, path:%s, thumbPath: %s]"
                , getFile().getUrl(), getFile().getThumbUrl(), getFile().getFilePath(), getFile().getThumbnailPath());
    }
}
