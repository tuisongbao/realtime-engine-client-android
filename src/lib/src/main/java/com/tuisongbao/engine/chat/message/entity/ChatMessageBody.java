package com.tuisongbao.engine.chat.message.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tuisongbao.engine.chat.event.event.ChatEventMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatMessage.TYPE;
import com.tuisongbao.engine.log.LogUtil;

import org.json.JSONObject;

public abstract class ChatMessageBody implements Parcelable {
    private static final String TAG = "com.tuisongbao.engine.ChatMessageBody";

    /**
     * The extra of JSONObject type would create 'namevaluepairs' field after serialization by gson.
     */
    protected JsonObject extra;

    public ChatMessageBody(TYPE type) {
        this.type = type;
    }

    public ChatMessageBody() {
    }

    protected TYPE type;

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public void setExtra(JsonObject extraInJson) {
        extra = extraInJson;
    }

    public void setExtra(JSONObject extraInJSON) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject extraInJson = (JsonObject)parser.parse(extraInJSON.toString());
            this.extra = extraInJson;
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
    }

    public JSONObject getExtra() {
        if (extra == null) {
            return null;
        }

        JSONObject extraInJSON = null;
        try {
            extraInJSON = new JSONObject(extra.toString());
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
        return extraInJSON;
    }

    public static ChatMessageBody createMessage(TYPE type) {
        ChatMessageBody body = null;
        if (type == null) {
            body = new ChatTextMessageBody();
        }
        if (type == TYPE.TEXT) {
            body = new ChatTextMessageBody();
        } else if (type == TYPE.IMAGE) {
            body = new ChatImageMessageBody();
            body.setType(TYPE.IMAGE);
        } else if (type == TYPE.VOICE) {
            body = new ChatVoiceMessageBody();
            body.setType(TYPE.VOICE);
        } else if (type == TYPE.EVENT) {
            body = new ChatEventMessageBody();
            body.setType(TYPE.EVENT);
        }
        return body;
    }

    @Override
    public String toString() {
        if (extra != null) {
            return extra.toString();
        }
        return "";
    }

    protected void writeToParcel(Parcel out) {
        if (extra != null) {
            out.writeString(extra.toString());
        } else {
            // Must write something to the Parcel, otherwise, it will throw error when read from it.
            out.writeString("");
        }
    }

    protected void readFromParcel(Parcel in) {
        String extraString = in.readString();
        if (extraString.length() > 0) {
            Gson gson = new Gson();
            setExtra(gson.fromJson(extraString, JsonObject.class));
        }
    }
}
