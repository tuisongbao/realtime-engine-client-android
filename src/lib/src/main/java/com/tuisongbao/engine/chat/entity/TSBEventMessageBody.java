package com.tuisongbao.engine.chat.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class TSBEventMessageBody extends TSBMessageBody {
    public static final String EVENT_TYPE = "type";
    public static final String EVENT_TARGET = "target";

    JsonObject event;

    public TSBEventMessageBody() {
        super(TSBMessage.TYPE.EVENT);
        event = new JsonObject();
    }

    public void setEvent(JsonObject event) {
        this.event = event;
    }

    public TSBChatEvent getEventType() {
        return TSBChatEvent.getType(event.get(EVENT_TYPE).getAsString());
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

    public static final Parcelable.Creator<TSBEventMessageBody> CREATOR =
            new Parcelable.Creator<TSBEventMessageBody>() {
        @Override
        public TSBEventMessageBody createFromParcel(Parcel in) {
            return new TSBEventMessageBody(in);
        }

        @Override
        public TSBEventMessageBody[] newArray(int size) {
            return new TSBEventMessageBody[size];
        }
    };

    private TSBEventMessageBody(Parcel in) {
        super(TSBMessage.TYPE.EVENT);
        readFromParcel(in);
    }
}
