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
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageFileContent;
import com.tuisongbao.engine.log.LogUtil;

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
                ChatMessageFileContent file = gson.fromJson(fileElement, ChatMessageFileContent.class);
                content.setFile(file);
            }

            JsonElement extraElement = bodyJson.get("extra");
            if (extraElement != null) {
                JsonObject extraInJson = bodyJson.get("extra").getAsJsonObject();
                content.setExtra(extraInJson);
            }
        } catch (Exception e) {
            LogUtil.error(TAG, e);
        }
        return content;
    }
}
