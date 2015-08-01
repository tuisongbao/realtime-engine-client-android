package com.tuisongbao.engine.chat.message.event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageBody;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageBodySerializer;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.engine.chat.serializer.TSBChatMessageTypeSerializer;
import com.tuisongbao.engine.chat.user.ChatType;
import com.tuisongbao.engine.common.event.BaseResponseEvent;
import com.tuisongbao.engine.util.StrUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageGetResponseEvent extends BaseResponseEvent<List<ChatMessage>> {

    @Override
    public List<ChatMessage> parse() {
        List<ChatMessage> list = new ArrayList<>();
        String data = getData();
        if (StrUtil.isEmpty(data)) {
            return list;
        } else {
            try {
                JSONArray json = new JSONArray(data);
                if (json.length() > 0) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(ChatType.class,
                            new TSBChatMessageChatTypeSerializer());
                    gsonBuilder.registerTypeAdapter(ChatMessage.TYPE.class,
                            new TSBChatMessageTypeSerializer());
                    gsonBuilder.registerTypeAdapter(ChatMessageBody.class,
                            new TSBChatMessageBodySerializer());
                    Gson gson = gsonBuilder.create();
                    for (int i = 0; i < json.length(); i++) {
                        JSONObject item = json.optJSONObject(i);
                        JSONObject content = item.optJSONObject("content");
                        if (content != null) {
                            String type = content.optString("type");
                            if (StrUtil.isEmpty(type)) {
                                continue;
                            } else {
                                ChatMessage.TYPE t = ChatMessage.TYPE.getType(type);
                                if (t != null) {
                                    ChatMessage message = ChatMessage.createMessage(t);
                                    message = gson.fromJson(item.toString(), ChatMessage.class);
                                    if (message != null) {
                                        list.add(message);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
