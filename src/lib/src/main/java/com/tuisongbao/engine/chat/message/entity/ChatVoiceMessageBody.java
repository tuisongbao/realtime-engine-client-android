package com.tuisongbao.engine.chat.message.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ChatVoiceMessageBody extends ChatMediaMessageBody {
    public static final String VOICE_INFO_DURATION = "duration";

    public ChatVoiceMessageBody() {
        super(ChatMessage.TYPE.VOICE);
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

    public static final Parcelable.Creator<ChatVoiceMessageBody> CREATOR = new Parcelable.Creator<ChatVoiceMessageBody>() {
        @Override
        public ChatVoiceMessageBody createFromParcel(Parcel in) {
            return new ChatVoiceMessageBody(in);
        }

        @Override
        public ChatVoiceMessageBody[] newArray(int size) {
            return new ChatVoiceMessageBody[size];
        }
    };

    public String getDuration() {
        try {
            return file.get(VOICE_INFO_DURATION).getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    public void setDuration(String duration) {
        file.addProperty(VOICE_INFO_DURATION, duration);
    }

    @Override
    public String toString() {
        return String.format(
                "ChatVoiceMessageBody[file: %s, type: %s, ChatMessageBody: %s]",
                file.toString(), type.getName(), super.toString());
    }

    private ChatVoiceMessageBody(Parcel in) {
        super(ChatMessage.TYPE.VOICE);
        readFromParcel(in);
    }
}
