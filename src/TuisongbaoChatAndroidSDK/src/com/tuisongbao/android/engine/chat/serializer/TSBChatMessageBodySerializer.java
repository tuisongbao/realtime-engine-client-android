package com.tuisongbao.android.engine.chat.serializer;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.entity.TSBMessageBody;

public class TSBChatMessageBodySerializer implements JsonDeserializer<TSBMessageBody> {

    @Override
    public TSBMessageBody deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext arg) throws JsonParseException {
        JsonObject bodyJson = json.getAsJsonObject();
        TSBMessage.TYPE type = TSBMessage.TYPE.TEXT;
        String text = "";
        if (bodyJson != null) {
            type = TSBMessage.TYPE.getType(bodyJson.get("type") != null ? bodyJson.get("type").getAsString() : null);
            text = bodyJson.get("text") != null ? bodyJson.get("text") .getAsString() : "";
        }
        TSBMessageBody body = TSBMessageBody.createMessage(type);
        body.setText(text);
        return body;
    }

}
