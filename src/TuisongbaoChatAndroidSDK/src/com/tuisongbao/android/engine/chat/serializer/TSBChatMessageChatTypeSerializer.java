package com.tuisongbao.android.engine.chat.serializer;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tuisongbao.android.engine.chat.entity.ChatType;

public class TSBChatMessageChatTypeSerializer implements
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
