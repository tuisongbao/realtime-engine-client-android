package com.tuisongbao.engine.chat.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tuisongbao.engine.chat.message.entity.content.ChatMessageEventContent;

import java.lang.reflect.Type;

/**
 * Created by root on 15-8-7.
 */
public class ChatMessageEventTypeSerializer implements
        JsonSerializer<ChatMessageEventContent.TYPE>, JsonDeserializer<ChatMessageEventContent.TYPE> {

    @Override
    public JsonElement serialize(ChatMessageEventContent.TYPE type, Type typeOfT,
                                 JsonSerializationContext arg) {
        return new JsonPrimitive(type.getName());
    }

    @Override
    public ChatMessageEventContent.TYPE deserialize(JsonElement json, Type typeOfT,
                                JsonDeserializationContext arg) throws JsonParseException {
        return ChatMessageEventContent.TYPE.getType(json.getAsString());
    }

}
