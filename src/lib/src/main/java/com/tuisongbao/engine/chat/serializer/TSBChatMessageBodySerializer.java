package com.tuisongbao.engine.chat.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tuisongbao.engine.chat.event.event.ChatEventMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatImageMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessage.TYPE;
import com.tuisongbao.engine.chat.message.entity.ChatMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatTextMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatVideoMessageBody;
import com.tuisongbao.engine.chat.message.entity.ChatVoiceMessageBody;

import java.lang.reflect.Type;

public class TSBChatMessageBodySerializer implements JsonDeserializer<ChatMessageBody> {

    @Override
    public ChatMessageBody deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext arg) throws JsonParseException {
        JsonObject bodyJson = json.getAsJsonObject();
        ChatMessage.TYPE type = ChatMessage.TYPE.TEXT;
        String text = "";
        if (bodyJson != null) {
            type = ChatMessage.TYPE.getType(bodyJson.get("type") != null ? bodyJson.get("type").getAsString() : null);
            text = bodyJson.get("text") != null ? bodyJson.get("text") .getAsString() : "";
        }

        ChatMessageBody messageBody = null;
        if (type == TYPE.TEXT) {
            ChatTextMessageBody textBody = new ChatTextMessageBody();
            textBody.setText(text);
            messageBody = textBody;

        } else if (type == TYPE.IMAGE) {
            ChatImageMessageBody imageBody = new ChatImageMessageBody();
            imageBody.setFile(bodyJson.get("file").getAsJsonObject());
            messageBody = imageBody;

        } else if (type == TYPE.VOICE) {
            ChatVoiceMessageBody voiceBody = new ChatVoiceMessageBody();
            voiceBody.setFile(bodyJson.get("file").getAsJsonObject());
            messageBody = voiceBody;
        } else if (type == TYPE.EVENT) {
            ChatEventMessageBody eventBody = new ChatEventMessageBody();
            eventBody.setEvent(bodyJson.get("event").getAsJsonObject());
            messageBody = eventBody;
        } else if (type == TYPE.VIDEO) {
            ChatVideoMessageBody videoBody = new ChatVideoMessageBody();
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
