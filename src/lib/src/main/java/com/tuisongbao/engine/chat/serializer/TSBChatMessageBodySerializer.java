package com.tuisongbao.engine.chat.serializer;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tuisongbao.engine.chat.entity.TSBEventMessageBody;
import com.tuisongbao.engine.chat.entity.TSBImageMessageBody;
import com.tuisongbao.engine.chat.entity.TSBMessage;
import com.tuisongbao.engine.chat.entity.TSBMessageBody;
import com.tuisongbao.engine.chat.entity.TSBTextMessageBody;
import com.tuisongbao.engine.chat.entity.TSBVideoMessageBody;
import com.tuisongbao.engine.chat.entity.TSBVoiceMessageBody;
import com.tuisongbao.engine.chat.entity.TSBMessage.TYPE;

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

        TSBMessageBody messageBody = null;
        if (type == TYPE.TEXT) {
            TSBTextMessageBody textBody = new TSBTextMessageBody();
            textBody.setText(text);
            messageBody = textBody;

        } else if (type == TYPE.IMAGE) {
            TSBImageMessageBody imageBody = new TSBImageMessageBody();
            imageBody.setFile(bodyJson.get("file").getAsJsonObject());
            messageBody = imageBody;

        } else if (type == TYPE.VOICE) {
            TSBVoiceMessageBody voiceBody = new TSBVoiceMessageBody();
            voiceBody.setFile(bodyJson.get("file").getAsJsonObject());
            messageBody = voiceBody;
        } else if (type == TYPE.EVENT) {
            TSBEventMessageBody eventBody = new TSBEventMessageBody();
            eventBody.setEvent(bodyJson.get("event").getAsJsonObject());
            messageBody = eventBody;
        } else if (type == TYPE.VIDEO) {
            TSBVideoMessageBody videoBody = new TSBVideoMessageBody();
            videoBody.setFile(bodyJson.get("file").getAsJsonObject());
            messageBody = videoBody;
        }

        if (bodyJson.get("extra") != null) {
            JsonObject extraInJson = bodyJson.get("extra").getAsJsonObject();
            messageBody.setExtra(extraInJson);
        }

        return messageBody;
    }

}
