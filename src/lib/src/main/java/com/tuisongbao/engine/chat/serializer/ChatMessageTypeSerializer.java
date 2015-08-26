package com.tuisongbao.engine.chat.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tuisongbao.engine.chat.message.ChatMessage;
import com.tuisongbao.engine.chat.message.ChatMessage.TYPE;

import java.lang.reflect.Type;

public class ChatMessageTypeSerializer implements
        JsonSerializer<ChatMessage.TYPE>, JsonDeserializer<ChatMessage.TYPE> {

    @Override
    public JsonElement serialize(TYPE type, Type typeOfT,
            JsonSerializationContext arg) {
        return new JsonPrimitive(type.getName());
    }

    @Override
    public TYPE deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext arg) throws JsonParseException {
        return ChatMessage.TYPE.getType(json.getAsString());
    }

}
