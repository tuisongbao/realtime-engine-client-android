package com.tuisongbao.engine.chat.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessage.TYPE;
import com.tuisongbao.engine.chat.message.entity.ChatMessageContent;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageEventEntity;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageFileEntity;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageLocationEntity;
import com.tuisongbao.engine.utils.LogUtils;

import java.lang.reflect.Type;

public class ChatMessageContentSerializer implements JsonDeserializer<ChatMessageContent> {
    private static final String TAG = ChatMessageContentSerializer.class.getSimpleName();

    @Override
    public ChatMessageContent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext arg) throws JsonParseException {
        Gson gson = new Gson();
        ChatMessageContent content = new ChatMessageContent();
        try {
            JsonObject bodyJson = json.getAsJsonObject();
            TYPE type = ChatMessage.TYPE.getType(bodyJson.get("type") != null ? bodyJson.get("type").getAsString() : null);
            content.setType(type);

            JsonElement textElement = bodyJson.get("text");
            if (textElement != null) {
                content.setText(textElement.getAsString());
            }

            JsonElement fileElement = bodyJson.get("file");
            if (fileElement != null) {
                ChatMessageFileEntity file = gson.fromJson(fileElement, ChatMessageFileEntity.class);
                content.setFile(file);
            }

            JsonElement eventElement = bodyJson.get("event");
            if (eventElement != null) {
                ChatMessageEventEntity event = gson.fromJson(eventElement, ChatMessageEventEntity.class);
                content.setEvent(event);
            }

            JsonElement locationElement = bodyJson.get("location");
            if (locationElement != null) {
                ChatMessageLocationEntity location = gson.fromJson(locationElement, ChatMessageLocationEntity.class);
                content.setLocation(location);
            }

            JsonElement extraElement = bodyJson.get("extra");
            if (extraElement != null) {
                JsonObject extraInJson = bodyJson.get("extra").getAsJsonObject();
                content.setExtra(extraInJson);
            }
        } catch (Exception e) {
            LogUtils.error(TAG, e);
        }
        return content;
    }
}
