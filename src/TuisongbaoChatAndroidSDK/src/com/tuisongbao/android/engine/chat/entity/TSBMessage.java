package com.tuisongbao.android.engine.chat.entity;

import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

import com.tuisongbao.android.engine.util.StrUtil;

public class TSBMessage implements Parcelable {
    /***
     * This value is not unique, it is the message's order number in a conversation,
     * A different conversation may has a message which has a same messageId.
     */
    private long messageId;
    private ChatType type = ChatType.SingleChat;
    private String from;
    private String to;
    private TSBMessageBody content;
    private String createdAt;
    private Map<String, String> map;

    public TSBMessage set(String key, String value) {
        if (this.map == null) {
            map = new HashMap<String, String>();
        }
        map.put(key, value);
        return this;
    }

    public ChatType getChatType() {
        return type;
    }

    public TSBMessage setChatType(ChatType type) {
        this.type = type;
        return this;
    }

    public long getMessageId() {
        return messageId;
    }

    public TSBMessage setMessageId(long messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public TSBMessage setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getRecipient() {
        return to;
    }

    public TSBMessage setRecipient(String to) {
        this.to = to;
        return this;
    }

    public TSBMessageBody getBody() {
        return content;
    }

    public TSBMessage setBody(TSBMessageBody body) {
        this.content = body;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public TSBMessage setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public static TSBMessage createMessage(TYPE type) {
        TSBMessage message = new TSBMessage();
        message.setBody(TSBMessageBody.createMessage(type));
        return message;
    }

    public static enum TYPE {
        TEXT("text", 1),
        IMAGE("image", 2);

        private String name;
        private int index;

        private TYPE(String name, int index) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getIndex() {
            return index;
        }

        public static TYPE getType(String name) {
            if (!StrUtil.isEmpty(name)) {
                TYPE[] types = values();
                for (TYPE type : types) {
                    if (type.getName().equals(name)) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(messageId);
        dest.writeString(type == null ? "" : type.getName());
        dest.writeString(from);
        dest.writeString(to);
        dest.writeString(createdAt);
        dest.writeParcelable(content, flags);
        dest.writeMap(map);
    }

    public void readFromParcel(Parcel in) {
        messageId = in.readLong();
        String nameType = in.readString();
        type = StrUtil.isEmpty(nameType) ? null : ChatType.getType(nameType);
        from = in.readString();
        to = in.readString();
        createdAt = in.readString();
        content = in.readParcelable(TSBMessageBody.class.getClassLoader());
        map = in.readHashMap(HashMap.class.getClassLoader());
    }

    public static final Parcelable.Creator<TSBMessage> CREATOR =
            new Parcelable.Creator<TSBMessage>() {
        @Override
        public TSBMessage createFromParcel(Parcel in) {
            return new TSBMessage(in);
        }

        @Override
        public TSBMessage[] newArray(int size) {
            return new TSBMessage[size];
        }
    };

    private TSBMessage(Parcel in) {
        readFromParcel(in);
    }

    public TSBMessage() {
        // empty
    }

    @Override
    public String toString() {
        return String.format("TSBMessage[messageId: %s, from: %s, to: %s, chatType: %s, content: %s, createdAt: %s]"
                , messageId, from, to, type.getName(), content, createdAt);
    }
}
