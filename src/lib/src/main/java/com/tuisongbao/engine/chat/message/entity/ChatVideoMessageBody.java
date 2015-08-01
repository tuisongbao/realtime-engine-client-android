package com.tuisongbao.engine.chat.message.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ChatVideoMessageBody extends ChatMediaMessageBody {
    public static final String VIDEO_INFO_DURATION = "duration";

    public ChatVideoMessageBody() {
        super(ChatMessage.TYPE.VIDEO);
    }

    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);

        Gson gson = new Gson();
        setFile(gson.fromJson(in.readString(), JsonObject.class));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int arg1) {
        writeToParcel(out);
        out.writeString(file.toString());
    }

    public static final Parcelable.Creator<ChatVideoMessageBody> CREATOR =
            new Parcelable.Creator<ChatVideoMessageBody>() {
        @Override
        public ChatVideoMessageBody createFromParcel(Parcel in) {
            return new ChatVideoMessageBody(in);
        }

        @Override
        public ChatVideoMessageBody[] newArray(int size) {
            return new ChatVideoMessageBody[size];
        }
    };

    public String getDuration() {
        try {
            return file.get(VIDEO_INFO_DURATION).getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    public void setDuration(String duration) {
        file.addProperty(VIDEO_INFO_DURATION, duration);
    }

    @Override
    public String toString() {
        return String.format("ChatVideoMessageBody[file: %s, type: %s, ChatMessageBody: %s]", file.toString(), type.getName(), super.toString());
    }

    private ChatVideoMessageBody(Parcel in) {
        super(ChatMessage.TYPE.VIDEO);
        readFromParcel(in);
    }
}
