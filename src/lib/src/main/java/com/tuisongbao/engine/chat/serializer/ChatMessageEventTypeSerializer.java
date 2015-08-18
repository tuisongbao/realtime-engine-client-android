package com.tuisongbao.engine.chat.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageEventEntity;

import java.lang.reflect.Type;

public class ChatMessageEventTypeSerializer implements
        JsonSerializer<ChatMessageEventEntity.TYPE>, JsonDeserializer<ChatMessageEventEntity.TYPE> {

    @Override
    public JsonElement serialize(ChatMessageEventEntity.TYPE type, Type typeOfT,
                                 JsonSerializationContext arg) {
        return new JsonPrimitive(type.getName());
    }

    @Override
    public ChatMessageEventEntity.TYPE deserialize(JsonElement json, Type typeOfT,
                                JsonDeserializationContext arg) throws JsonParseException {
        return ChatMessageEventEntity.TYPE.getType(json.getAsString());
    }

}
