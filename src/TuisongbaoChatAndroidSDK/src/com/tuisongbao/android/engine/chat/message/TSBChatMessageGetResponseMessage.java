package com.tuisongbao.android.engine.chat.message;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tuisongbao.android.engine.TSBEngine;
import com.tuisongbao.android.engine.chat.TSBChatManager;
import com.tuisongbao.android.engine.chat.db.TSBConversationDataSource;
import com.tuisongbao.android.engine.chat.entity.ChatType;
import com.tuisongbao.android.engine.chat.entity.TSBChatMessageGetData;
import com.tuisongbao.android.engine.chat.entity.TSBChatUser;
import com.tuisongbao.android.engine.chat.entity.TSBMessage;
import com.tuisongbao.android.engine.chat.entity.TSBMessageBody;
import com.tuisongbao.android.engine.chat.serializer.TSBChatMessageBodySerializer;
import com.tuisongbao.android.engine.chat.serializer.TSBChatMessageChatTypeSerializer;
import com.tuisongbao.android.engine.chat.serializer.TSBChatMessageTypeSerializer;
import com.tuisongbao.android.engine.common.BaseTSBResponseMessage;
import com.tuisongbao.android.engine.util.StrUtil;

public class TSBChatMessageGetResponseMessage extends BaseTSBResponseMessage<List<TSBMessage>> {

    @Override
    protected List<TSBMessage> prepareCallBackData() {
        List<TSBMessage> messages = super.prepareCallBackData();

        if (!TSBChatManager.getInstance().isCacheEnabled()) {
            return messages;
        }

        TSBChatUser user = TSBChatManager.getInstance().getChatUser();
        TSBConversationDataSource dataSource = new TSBConversationDataSource(TSBEngine.getContext());
        dataSource.open();
        for (TSBMessage message : messages) {
            dataSource.upsertMessage(user.getUserId(), message);
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ChatType.class, new TSBChatMessageChatTypeSerializer());
        TSBChatMessageGetData requestData = gsonBuilder.create().fromJson((String)getRequestData(),
                new TypeToken<TSBChatMessageGetData>() {
                }.getType());
        messages = dataSource.getMessages(requestData.getType(), requestData.getTarget(), requestData.getStartMessageId(), requestData.getEndMessageId());
        dataSource.close();

        return messages;
    }

    @Override
    public List<TSBMessage> parse() {
        List<TSBMessage> list = new ArrayList<TSBMessage>();
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
                    gsonBuilder.registerTypeAdapter(TSBMessage.TYPE.class,
                            new TSBChatMessageTypeSerializer());
                    gsonBuilder.registerTypeAdapter(TSBMessageBody.class,
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
                                TSBMessage.TYPE t = TSBMessage.TYPE.getType(type);
                                if (t != null) {
                                    TSBMessage message = TSBMessage.createMessage(t);
                                    message = gson.fromJson(item.toString(), TSBMessage.class);
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
