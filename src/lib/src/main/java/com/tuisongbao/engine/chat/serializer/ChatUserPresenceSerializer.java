package com.tuisongbao.engine.chat.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.tuisongbao.engine.chat.ChatUserPresence;

import java.lang.reflect.Type;

/**
 * {@link ChatUserPresence.Presence} 的序列化工具
 *
 * <P>
 *     序列化：{@code Presence.OFFLINE} 转变为其 {@code name} "offline"
 *     反序列化：将 "offline" 转变为 {@code Presence.OFFLINE}
 */
public class ChatUserPresenceSerializer implements JsonSerializer<ChatUserPresence.Presence>,
        JsonDeserializer<ChatUserPresence.Presence> {
    @Override
    public JsonElement serialize(ChatUserPresence.Presence src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getName());
    }

    @Override
    public ChatUserPresence.Presence deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return ChatUserPresence.Presence.getPresence(json.getAsString());
    }
}
