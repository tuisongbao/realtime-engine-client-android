package com.tuisongbao.engine.chat.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tuisongbao.engine.chat.ChatType;

import java.lang.reflect.Type;

public class ChatTypeSerializer implements
        JsonSerializer<ChatType>, JsonDeserializer<ChatType> {

    @Override
    public JsonElement serialize(ChatType type, Type typeOfT,
            JsonSerializationContext arg) {
        return new JsonPrimitive(type.getName());
    }

    @Override
    public ChatType deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext arg) throws JsonParseException {
        return ChatType.getType(json.getAsString());
    }

}