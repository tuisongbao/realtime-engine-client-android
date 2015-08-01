package com.tuisongbao.engine.chat.event.event;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tuisongbao.engine.chat.event.entity.ChatEvent;
import com.tuisongbao.engine.chat.message.entity.ChatMessage;
import com.tuisongbao.engine.chat.message.entity.ChatMessageBody;

public class ChatEventMessageBody extends ChatMessageBody {
    public static final String EVENT_TYPE = "type";
    public static final String EVENT_TARGET = "target";

    JsonObject event;

    public ChatEventMessageBody() {
        super(ChatMessage.TYPE.EVENT);
        event = new JsonObject();
    }

    public void setEvent(JsonObject event) {
        this.event = event;
    }

    public ChatEvent getEventType() {
        return ChatEvent.getType(event.get(EVENT_TYPE).getAsString());
    }

    public String getEventTarget() {
        return event.get(EVENT_TARGET).getAsString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flag) {
        out.writeString(event.toString());
    }

    public void readFromParcel(Parcel in) {
        Gson gson = new Gson();
        setEvent(gson.fromJson(in.readString(), JsonObject.class));
    }

    public static final Parcelable.Creator<ChatEventMessageBody> CREATOR =
            new Parcelable.Creator<ChatEventMessageBody>() {
        @Override
        public ChatEventMessageBody createFromParcel(Parcel in) {
            return new ChatEventMessageBody(in);
        }

        @Override
        public ChatEventMessageBody[] newArray(int size) {
            return new ChatEventMessageBody[size];
        }
    };

    private ChatEventMessageBody(Parcel in) {
        super(ChatMessage.TYPE.EVENT);
        readFromParcel(in);
    }
}
